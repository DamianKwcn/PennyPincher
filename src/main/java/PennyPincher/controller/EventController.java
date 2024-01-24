package PennyPincher.controller;

import PennyPincher.dto.event.EventDto;
import PennyPincher.dto.event.EventMapper;
import PennyPincher.entity.Event;
import PennyPincher.entity.User;
import PennyPincher.service.events.EventService;
import PennyPincher.service.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final EventMapper eventMapper;

    @GetMapping("/events")
    public String events(@RequestParam(name = "eventName", required = false) String eventName,
                         Model model) {
        List<Event> events;

        if (eventName != null && !eventName.isEmpty()) {
            events = eventService.findEventsByName(eventName);
        } else {
            events = eventService.findAllEvents();
        }

        model.addAttribute("events", events);
        model.addAttribute("loggedInUserName", userService.getCurrentlyLoggedInUser().getUsername());
        return "events";
    }

    @GetMapping("/events/{eventId}")
    public String eventDetails(@PathVariable("eventId") Integer eventId,
                               Model model) {
        Event event = eventService.findById(eventId);
        model.addAttribute("event", event);
        return "event";
    }

    @GetMapping("/newEvent")
    public String showEventAddingForm(Model model) {
        List<User> allUsers = userService.findAllUsers();
        model.addAttribute("newEvent", new EventDto());
        model.addAttribute("loggedInUserName", userService.getCurrentlyLoggedInUser().getUsername());
        return "new-event";
    }

    @PostMapping("/newEvent")
    public String createEvent(@ModelAttribute("newEvent") EventDto eventDto,
                              Model model,
                              BindingResult result) {
        Event existingEvent = eventService.findByEventName(eventDto.getEventName());

        if (doesEventAlreadyExist(existingEvent)) {
            result.addError(new FieldError("newEvent", "eventName",
                    "Event '" + existingEvent.getEventName() + "' already exists."));
        }

        if (eventDto.getEventName().isBlank()) {
            result.addError(new FieldError("newEvent", "eventName",
                    "Event name field cannot be empty."));
        }

        if (result.hasErrors()) {
            return "new-event";
        }

        eventService.save(eventMapper.mapToDomain(eventDto));
        model.addAttribute("loggedInUserName", userService.getCurrentlyLoggedInUser().getUsername());
        return "redirect:/events";
    }

    @PostMapping("/delete")
    public String deleteEvent(@RequestParam("eventId") Integer eventId,
                              Model model) {
        List<Event> events = eventService.findAllEvents();
        Event event = eventService.findById(eventId);
        User user = userService.getCurrentlyLoggedInUser();

        if (event.getOwner().equals(user)) {
            eventService.deleteById(eventId);
        }

        model.addAttribute("events", events);
        model.addAttribute("loggedInUserName", user.getUsername());
        return "redirect:/events";
    }

    private boolean doesEventAlreadyExist(Event existingEvent) {
        return existingEvent != null && !existingEvent.getEventName().isBlank();
    }

    }
