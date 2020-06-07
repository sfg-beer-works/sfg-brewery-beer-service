package guru.sfg.brewery.beer_service.services.inventory;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.codec.DecodeException;
import guru.sfg.brewery.model.BeerInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by jt on 3/4/20.
 */
@Slf4j
@RequiredArgsConstructor
@Profile({"local-discovery", "digitalocean"})
@Service
public class BeerInventoryServiceFeign implements BeerInventoryService {
    private final InventoryServiceFeignClient inventoryServiceFeignClient;

    @Override
    public Integer getOnhandInventory(UUID beerId) {
        log.debug("Calling Inventory Service w/Feign - BeerId: " + beerId);

        int onHand = 0;

        try {
            ResponseEntity<List<BeerInventoryDto>> responseEntity = inventoryServiceFeignClient.getOnhandInventory(beerId);

            if (responseEntity.getBody() != null && responseEntity.getBody().size() > 0) {
                log.debug("Inventory found, summing inventory");

                onHand = Objects.requireNonNull(responseEntity.getBody())
                        .stream()
                        .mapToInt(BeerInventoryDto::getQuantityOnHand)
                        .sum();
            }
        } catch (Exception e) {
            log.error("Exception thrown calling inventory service");
            log.error(e.getClass().getCanonicalName());

            if(e instanceof HystrixRuntimeException){
                HystrixRuntimeException hre = (HystrixRuntimeException) e;
                log.error("HRE Error: " + hre.getCause().getLocalizedMessage());
                log.error("HRE Cause: " + hre.getCause().getClass().getCanonicalName());

                if (hre.getCause() instanceof DecodeException){
                    log.error("Decode Exception");
                    DecodeException de = (DecodeException) hre.getCause();
                    log.error("Content: " + de.contentUTF8());
                    log.error("de cause: " + de.getCause().getClass().getCanonicalName());

                    if (de.getCause() instanceof RestClientException){
                        RestClientException rce = (RestClientException) de.getCause();
                        Throwable root = rce.getRootCause();
                        log.error("Root cuase type " + root.getClass().getCanonicalName());
                        log.error("Root Stack: ", root);

                        if (root instanceof MismatchedInputException){
                            log.error("Is Mistamated ");
                            MismatchedInputException mie = (MismatchedInputException) root;
                            log.error("Original Message: " + mie.getOriginalMessage());
                            log.error("Mismatched Exception:", mie.getCause());
                            log.error("to string: " + mie.toString());

                        }
                    }
//                    HttpMessageNotReadableException httpe = (HttpMessageNotReadableException) hre.getCause();
//                    log.error("Feign Client returned status: ", httpe.getHttpInputMessage().getHeaders().toString());
//                    try {
//                        log.error(IOUtils.toString(httpe.getHttpInputMessage().getBody(), StandardCharsets.UTF_8));
//                    } catch (IOException ioException) {
//                        log.error("Failed to read body");
//                        ioException.printStackTrace();
//                    }
                }

                if (hre.getCause() instanceof MismatchedInputException){
                    MismatchedInputException mie = (MismatchedInputException) hre.getCause();
                    log.error("Original Message");
                    log.error(mie.getOriginalMessage());
                }
            }

            if (e instanceof HttpMessageNotReadableException){
                HttpMessageNotReadableException ex = (HttpMessageNotReadableException) e;
                log.error("Feign Client returned status: ", ex.getHttpInputMessage().getHeaders().toString());
                try {
                    log.error(IOUtils.toString(ex.getHttpInputMessage().getBody(), StandardCharsets.UTF_8));
                } catch (IOException ioException) {
                    log.error("Failed to read body");
                    ioException.printStackTrace();
                }
            }
            log.error(e.getMessage());
            log.error("Error in calling Inventory ", e);
            throw e;
        }



        log.debug("BeerId: " + beerId + " On hand is: " + onHand);

        return onHand;
    }
}
