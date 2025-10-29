package org.userregistrationspringsecurity.event;

import org.springframework.context.ApplicationEvent;
import org.userregistrationspringsecurity.entity.User;

public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private final User user;

    public OnRegistrationCompleteEvent(User user) {
        super(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
