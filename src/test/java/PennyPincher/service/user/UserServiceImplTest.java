package PennyPincher.service.user;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.SplitExpenseDto;
import PennyPincher.model.Event;
import PennyPincher.model.Expense;
import PennyPincher.model.User;
import PennyPincher.repository.UserRepository;
import PennyPincher.service.users.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    private UserServiceImpl userService;

    User user1 = User.builder().id(1).firstName("user1").username("user1").password("password").build();
    User user2 = User.builder().id(2).firstName("user2").username("user2").password("password").build();

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void Should_CalculateTotalBalanceForUser() {
        // Given
        Map<Event, BigDecimal> balanceInEachEvent = new HashMap<>();
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);

        when(event1.getEventMembers()).thenReturn(List.of(user1));
        when(event2.getEventMembers()).thenReturn(List.of(user1));

        balanceInEachEvent.put(event1, BigDecimal.valueOf(100));
        balanceInEachEvent.put(event2, BigDecimal.valueOf(200));

        // When
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        BigDecimal totalBalance = userService.totalBalanceForUser(user1, balanceInEachEvent);

        // Then
        assertEquals(BigDecimal.valueOf(300), totalBalance);
    }

    @Test
    public void testBalanceInEachEvent() {
        // Given
        Event event1 = new Event();
        Event event2 = new Event();

        event1.setEventName("Event 1");
        event2.setEventName("Event 2");

        List<Event> events = Arrays.asList(event1, event2);

        Expense expense1 = mock(Expense.class);
        Expense expense2 = mock(Expense.class);

        when(expense1.getEvent()).thenReturn(event1);
        when(expense2.getEvent()).thenReturn(event2);
        when(expense1.getParticipants()).thenReturn(Collections.singletonList(user1));
        when(expense2.getParticipants()).thenReturn(Collections.singletonList(user1));
        when(expense1.getCostPerUser()).thenReturn(Collections.singletonMap(user1.getId(), BigDecimal.valueOf(100)));
        when(expense2.getCostPerUser()).thenReturn(Collections.singletonMap(user1.getId(), BigDecimal.valueOf(200)));
        when(expense1.getPayoffPerUser()).thenReturn(Collections.singletonMap(user1.getId(), BigDecimal.valueOf(50)));
        when(expense2.getPayoffPerUser()).thenReturn(Collections.singletonMap(user1.getId(), BigDecimal.valueOf(100)));

        Set<Expense> expenses = new HashSet<>(Arrays.asList(expense1, expense2));

        // When
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        Map<Event, BigDecimal> balanceMap = userService.balanceInEachEvent(user1, events, expenses);

        // Then
        assertEquals(BigDecimal.valueOf(-50), balanceMap.get(event1));
        assertEquals(BigDecimal.valueOf(-100), balanceMap.get(event2));
    }

    @Test
    public void Should_ReturnAllUsers_When_GivenUserList() {
        // given
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);
        when(userRepository.findAll()).thenReturn(userList);

        // when
        List<User> result = userService.findAll();

        // then
        assertEquals(userList.size(), result.size());
        for (int i = 0; i < userList.size(); i++) {
            assertEquals(userList.get(i).getId(), result.get(i).getId());
            assertEquals(userList.get(i).getFirstName(), result.get(i).getFirstName());
            assertEquals(userList.get(i).getUsername(), result.get(i).getUsername());
            assertEquals(userList.get(i).getPassword(), result.get(i).getPassword());
        }
    }

    @Test
    public void Should_ReturnUsersByNames_When_GivenCustomExpenseDto() {
        // given
        CustomExpenseDto customExpenseDto = new CustomExpenseDto();
        customExpenseDto.setParticipantsNames(Arrays.asList("user1", "user2"));

        List<User> userList = Arrays.asList(user1, user2);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));

        // when
        List<User> result = userService.getUsersByNames(customExpenseDto);

        // then
        assertEquals(userList.size(), result.size());
        assertEquals(userList, result);
    }

    @Test
    public void Should_ReturnUsersByNames_When_GivenSplitExpenseDto() {
        // given
        SplitExpenseDto splitExpenseDto = new SplitExpenseDto();
        splitExpenseDto.setParticipantUsername("user1,user2");

        List<User> userList = Arrays.asList(user1, user2);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));

        // when
        List<User> result = userService.getUsersByNames(splitExpenseDto);

        // then
        assertEquals(userList.size(), result.size());
        assertEquals(userList, result);
    }
}
