package PennyPincher.service.payoff;

import PennyPincher.model.Expense;
import PennyPincher.model.Payoff;
import PennyPincher.model.User;
import PennyPincher.repository.PayoffRepository;
import PennyPincher.service.payoffs.PayoffServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PayoffServiceImplTest {

    @Mock
    private PayoffRepository payoffRepository;

    @Test
    public void testCreatePayoff() {
        // given
        Expense foundExpense = new Expense();
        User foundUser = Mockito.mock(User.class);
        BigDecimal paidOffFromInput = BigDecimal.valueOf(50);
        List<Payoff> payoffsList = new ArrayList<>();

        // when
        Mockito.when(foundUser.getPayoffs()).thenReturn(payoffsList);
        PayoffServiceImpl payoffService = new PayoffServiceImpl(payoffRepository);
        Payoff payoff = payoffService.createPayoff(foundExpense, foundUser, paidOffFromInput);

        // then
        assertEquals(foundExpense, payoff.getExpensePaid());
        assertEquals(foundUser, payoff.getUserPaying());
        assertEquals(paidOffFromInput, payoff.getPayoffAmount());
    }
}

