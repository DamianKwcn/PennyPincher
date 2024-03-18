package PennyPincher.service.payoffs;

import PennyPincher.entity.Payoff;
import PennyPincher.repository.PayoffRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PayoffServiceImpl implements PayoffService {

    private final PayoffRepository payoffRepository;

    @Override
    public void save(Payoff payoff) {
        payoffRepository.save(payoff);
    }

}
