package PennyPincher.service.expense;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.ExpenseMapper;
import PennyPincher.model.Event;
import PennyPincher.model.Expense;
import PennyPincher.model.Payoff;
import PennyPincher.model.User;
import PennyPincher.repository.ExpenseRepository;
import PennyPincher.service.expenses.ExpenseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    User user1 = User.builder().id(1).firstName("user1").username("user1").password("password").build();
    User user2 = User.builder().id(2).firstName("user2").username("user2").password("password").build();

    @Test
    public void Should_ReturnExpensesForGivenEvent() {
        // given
        Integer eventId = 1;
        Event event = new Event();
        event.setId(eventId);

        Expense expense1 = new Expense();
        expense1.setId(1);
        expense1.setEvent(event);
        Expense expense2 = new Expense();
        expense2.setId(2);
        expense2.setEvent(event);
        List<Expense> expenses = Arrays.asList(expense1, expense2);

        // when
        when(expenseRepository.findByEventId(eventId)).thenReturn(expenses);
        List<Expense> result = expenseService.findExpensesForGivenEvent(eventId);

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(expense1));
        assertTrue(result.contains(expense2));
    }

    @Test
    public void Should_SplitCostEquallyPerParticipants() {
        // given
        BigDecimal amount = BigDecimal.valueOf(100.00);
        long participantsNumber = 4;
        BigDecimal expectedSplit = new BigDecimal("25.00");

        // when
        BigDecimal result = expenseService.splitCostEquallyPerParticipants(amount, participantsNumber);

        // then
        assertEquals(expectedSplit, result);
    }

    @Test
    public void Should_ReturnZero_When_ThereAreNoParticipants() {
        // given
        BigDecimal amount = BigDecimal.valueOf(100);
        long participantsNumber = 0;
        BigDecimal expectedSplit = BigDecimal.ZERO;

        // when
        BigDecimal result = expenseService.splitCostEquallyPerParticipants(amount, participantsNumber);

        // then
        assertEquals(expectedSplit, result);
    }

    @Test
    public void Should_MapUserToCost() {
        // given
        Expense expense = new Expense();
        expense.setParticipants(Arrays.asList(user1, user2));
        expense.setCostPerUser(Map.of(
                1, BigDecimal.valueOf(50),
                2, BigDecimal.valueOf(30)
        ));

        // when
        Map<Integer, BigDecimal> result = expenseService.mapUserToCost(expense);

        // then
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(50), result.get(1));
        assertEquals(BigDecimal.valueOf(30), result.get(2));
    }

    @Test
    public void Should_MapUserToPayoffAmount() {
        // given
        Expense expense = new Expense();
        expense.setParticipants(Arrays.asList(user1, user2));

        Payoff payoff1 = new Payoff();
        payoff1.setUserPaying(user1);
        payoff1.setPayoffAmount(BigDecimal.valueOf(20));

        Payoff payoff2 = new Payoff();
        payoff2.setUserPaying(user2);
        payoff2.setPayoffAmount(BigDecimal.valueOf(30));

        expense.setPayoffs(Arrays.asList(payoff1, payoff2));

        // when
        Map<Integer, BigDecimal> result = expenseService.mapUserToPayoffAmount(expense);

        // then
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(20), result.get(1));
        assertEquals(BigDecimal.valueOf(30), result.get(2));
    }

    @Test
    public void Should_MapUserToBalance() {
        // given
        Expense expense = new Expense();
        expense.setParticipants(Arrays.asList(user1, user2));

        Payoff payoff1 = new Payoff();
        payoff1.setUserPaying(user1);
        payoff1.setPayoffAmount(BigDecimal.valueOf(-20));

        Payoff payoff2 = new Payoff();
        payoff2.setUserPaying(user2);
        payoff2.setPayoffAmount(BigDecimal.valueOf(-30));

        expense.setPayoffs(Arrays.asList(payoff1, payoff2));

        // when
        Map<Integer, BigDecimal> result = expenseService.mapUserToBalance(expense);

        // then
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(-20), result.get(1)); //
        assertEquals(BigDecimal.valueOf(-30), result.get(2)); //
    }

    @Test
    public void Should_FindExpense_When_GivenExpenseNameAndEventId() {
        // given
        String expenseName = "Expense";
        Integer eventId = 1;
        Expense expectedExpense = new Expense();

        // when
        when(expenseRepository.findByNameAndEventId(expenseName, eventId)).thenReturn(expectedExpense);
        Optional<Expense> result = Optional.ofNullable(expenseService.findByExpenseNameAndEventId(expenseName, eventId));

        // then
        assertTrue(result.isPresent());
        assertEquals(expectedExpense, result.get());
    }

    @Test
    public void Should_SumParticipantPayoffs() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // given
        User participant = user1;
        Payoff payoff1 = new Payoff();
        payoff1.setUserPaying(participant);
        payoff1.setPayoffAmount(BigDecimal.valueOf(20));

        Payoff payoff2 = new Payoff();
        payoff2.setUserPaying(participant);
        payoff2.setPayoffAmount(BigDecimal.valueOf(30));

        Expense expense = new Expense();
        expense.setPayoffs(Arrays.asList(payoff1, payoff2));

        // when
        Method method = ExpenseServiceImpl.class.getDeclaredMethod("sumParticipantPayoffs", Expense.class, User.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal) method.invoke(expenseService, expense, participant);

        // then
        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    public void Should_SumParticipantCosts() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // given
        User participant = user1;
        Expense expense = new Expense();
        Map<Integer, BigDecimal> costPerUser = new HashMap<>();
        costPerUser.put(participant.getId(), BigDecimal.valueOf(50));
        expense.setCostPerUser(costPerUser);

        // when
        Method method = ExpenseServiceImpl.class.getDeclaredMethod("sumParticipantCosts", Expense.class, User.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal) method.invoke(expenseService, expense, participant);

        // then
        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    public void Should_CalculateUpdatedBalance_ForGivenEvent() {
        // given
        Expense expense1 = new Expense();
        Expense expense2 = new Expense();
        expense1.setBalancePerUser(Map.of(
                1, BigDecimal.valueOf(20),
                2, BigDecimal.valueOf(30)
        ));

        expense2.setBalancePerUser(Map.of(
                1, BigDecimal.valueOf(10),
                2, BigDecimal.valueOf(15)
        ));

        List<Expense> eventExpenses = new ArrayList<>();
        eventExpenses.add(expense1);
        eventExpenses.add(expense2);

        BigDecimal expectedTotalBalance = BigDecimal.valueOf(75);

        // when
        BigDecimal result = expenseService.calculateUpdatedBalanceForEvent(eventExpenses);

        // then
        assertEquals(expectedTotalBalance, result);
    }

    @Test
    public void Should_CreateExpense(){
        // given
        Event foundEvent = new Event();
        User loggedInUser = new User();
        CustomExpenseDto customExpenseDto = new CustomExpenseDto();
        customExpenseDto.setCost("100");
        customExpenseDto.setName("Test Expense");

        ExpenseMapper expenseMapper = mock(ExpenseMapper.class);

        when(expenseMapper.mapCustomExpenseDtoToDomain(eq(foundEvent), eq(customExpenseDto))).thenReturn(
                Expense.builder()
                        .name(customExpenseDto.getName())
                        .totalCost(new BigDecimal(customExpenseDto.getCost()))
                        .payoffs(Collections.singletonList(Payoff.builder().userPaying(loggedInUser).build()))
                        .build()
        );

        // when
        Expense createdExpense = expenseService.createExpense(foundEvent, loggedInUser, customExpenseDto, expenseMapper);

        // then
        assertNotNull(createdExpense);
        assertEquals("Test Expense", createdExpense.getName());
        assertEquals(BigDecimal.valueOf(100), createdExpense.getTotalCost());
        assertEquals(1, createdExpense.getPayoffs().size());
        assertEquals(loggedInUser, createdExpense.getPayoffs().get(0).getUserPaying());
    }

    @Test
    public void Should_CalculateParticipantBalance() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // given
        User participant = new User();
        participant.setId(1);

        Payoff payoff1 = new Payoff();
        payoff1.setUserPaying(participant);
        payoff1.setPayoffAmount(BigDecimal.valueOf(20));

        Payoff payoff2 = new Payoff();
        payoff2.setUserPaying(participant);
        payoff2.setPayoffAmount(BigDecimal.valueOf(30));

        Map<Integer, BigDecimal> costPerUser = new HashMap<>();
        costPerUser.put(participant.getId(), BigDecimal.valueOf(50));

        Expense expense = new Expense();
        expense.setPayoffs(Arrays.asList(payoff1, payoff2));
        expense.setCostPerUser(costPerUser);

        // when
        Method method = ExpenseServiceImpl.class.getDeclaredMethod("calculateParticipantBalance", Expense.class, User.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal) method.invoke(expenseService, expense, participant);

        // then
        assertEquals(BigDecimal.ZERO, result);
    }

}
