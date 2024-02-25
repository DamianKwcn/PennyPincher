package PennyPincher.service.user;

import PennyPincher.dto.expense.CustomExpenseDto;
import PennyPincher.dto.expense.SplitExpenseDto;
import PennyPincher.entity.User;
import PennyPincher.repository.UserRepository;
import PennyPincher.service.users.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    User user1 = User.builder().id(1).firstName("user1").username("user1").password("password").build();
    User user2 = User.builder().id(2).firstName("user2").username("user2").password("password").build();

    @Before
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
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
        customExpenseDto.setParticipantsNames(Arrays.asList("Amadeus", "Benjamin"));

        User user1 = new User();
        User user2 = new User();
        user1.setUsername("Amadeus");
        user2.setUsername("Benjamin");
        List<User> userList = Arrays.asList(user1, user2);

        when(userRepository.findByUsername("Amadeus")).thenReturn(user1);
        when(userRepository.findByUsername("Benjamin")).thenReturn(user2);

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
        splitExpenseDto.setParticipantUsername("Amadeus,Benjamin");

        User user1 = new User();
        User user2 = new User();
        user1.setUsername("Amadeus");
        user2.setUsername("Benjamin");
        List<User> userList = Arrays.asList(user1, user2);

        when(userRepository.findByUsername("Amadeus")).thenReturn(user1);
        when(userRepository.findByUsername("Benjamin")).thenReturn(user2);

        // when
        List<User> result = userService.getUsersByNames(splitExpenseDto);

        // then
        assertEquals(userList.size(), result.size());
        assertEquals(userList, result);
    }
}
