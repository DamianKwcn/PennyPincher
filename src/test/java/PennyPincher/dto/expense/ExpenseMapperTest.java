package PennyPincher.dto.expense;

import PennyPincher.entity.Event;
import PennyPincher.entity.Expense;
import PennyPincher.entity.User;
import PennyPincher.service.expenses.ExpenseService;
import PennyPincher.service.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ExpenseMapperTest {

    @Mock
    private UserService userService;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseMapper expenseMapper;

    @Test
    public void Should_MapSplitExpenseToDomain() {
        // given
        Event event = new Event();
        SplitExpenseDto splitExpenseDto = new SplitExpenseDto();
        splitExpenseDto.setName("Test Expense");
        splitExpenseDto.setCost("100");
        splitExpenseDto.setParticipantUsername("user1");

        User user1 = User.builder()
                .id(1)
                .firstName("user1")
                .username("user1")
                .password("password")
                .balance(BigDecimal.valueOf(0))
                .expenses(new HashSet<>())
                .build();

        List<User> expenseParticipants = Collections.singletonList(user1);

        BigDecimal cost = BigDecimal.valueOf(100);
        BigDecimal equalPerEachParticipant = BigDecimal.valueOf(100);

        // when
        when(userService.getUsersByNames(splitExpenseDto)).thenReturn(expenseParticipants);
        when(expenseService.splitCostEquallyPerParticipants(cost, expenseParticipants.size())).thenReturn(equalPerEachParticipant);
        Expense result = expenseMapper.mapSplitExpenseToDomain(event, splitExpenseDto);

        // then
        assertNotNull(result);
        assertEquals(splitExpenseDto.getName(), result.getName());
        assertEquals(cost, result.getTotalCost());
        assertEquals(cost.negate(), result.getExpenseBalance());
        assertEquals(event, result.getEvent());
        assertEquals(expenseParticipants, result.getParticipants());
        assertEquals(1, result.getCostPerUser().size());
        assertEquals(equalPerEachParticipant, result.getCostPerUser().get(user1.getId()));
        assertTrue(result.getPayoffPerUser().isEmpty());
        assertTrue(result.getBalancePerUser().isEmpty());
    }

    @Test
    public void Should_MapCustomExpenseDtoToDomain() {
        // given
        Event event = new Event();
        CustomExpenseDto customExpenseDto = new CustomExpenseDto();
        customExpenseDto.setName("Test Expense");
        customExpenseDto.setCost("100");
        Map<Integer, BigDecimal> userContribution = new HashMap<>();
        userContribution.put(1, BigDecimal.valueOf(50));
        userContribution.put(2, BigDecimal.valueOf(30));
        customExpenseDto.setUserContribution(userContribution);

        User user1 = User.builder()
                .id(1)
                .firstName("user1")
                .username("user1")
                .password("password")
                .balance(BigDecimal.ZERO)
                .expenses(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2)
                .firstName("user2")
                .username("user2")
                .password("password")
                .balance(BigDecimal.ZERO)
                .expenses(new HashSet<>())
                .build();

        List<User> expenseParticipants = Arrays.asList(user1, user2);

        BigDecimal cost = BigDecimal.valueOf(100);

        // when
        when(userService.getUsersByNames(customExpenseDto)).thenReturn(expenseParticipants);
        Expense result = expenseMapper.mapCustomExpenseDtoToDomain(event, customExpenseDto);

        // then
        assertNotNull(result);
        assertEquals(customExpenseDto.getName(), result.getName());
        assertEquals(cost, result.getTotalCost());
        assertEquals(cost.negate(), result.getExpenseBalance());
        assertEquals(event, result.getEvent());
        assertEquals(expenseParticipants, result.getParticipants());
        assertEquals(userContribution, result.getCostPerUser());
        assertTrue(result.getPayoffPerUser().isEmpty());
        assertTrue(result.getBalancePerUser().isEmpty());
        assertEquals(BigDecimal.valueOf(-50), user1.getBalance());
        assertEquals(BigDecimal.valueOf(-30), user2.getBalance());
        verify(userService, times(2)).save(any(User.class));
    }
}