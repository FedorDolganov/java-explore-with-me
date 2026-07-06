package ru.practicum.mainserver.events;

public enum EventState {

    PENDING,
    PUBLISHED,
    CANCELED;


    public static boolean isCanceledOrPending(EventState state) {
        return state == PENDING || state == CANCELED;
    }


}
