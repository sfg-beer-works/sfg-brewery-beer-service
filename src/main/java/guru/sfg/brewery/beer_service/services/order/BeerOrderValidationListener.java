package guru.sfg.brewery.beer_service.services.order;

import guru.sfg.brewery.beer_service.config.JmsConfig;
import guru.sfg.brewery.model.events.BeerOrderValidationResult;
import guru.sfg.brewery.model.events.ValidateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2019-09-08.
 */
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final BeerOrderValidator beerOrderValidator;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(ValidateBeerOrderRequest event){

        Boolean orderIsValid = beerOrderValidator.validateOrder(event.getBeerOrder());

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT_QUEUE, BeerOrderValidationResult.builder()
                .beerOrderId(event.getBeerOrder().getId())
                .isValid(orderIsValid)
                .build());
    }
}
