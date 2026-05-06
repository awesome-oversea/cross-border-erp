package com.aidotnet.erp.common.context;

public final class ActorContext {

    private static final ThreadLocal<String> ACTOR_ID = new ThreadLocal<>();

    private ActorContext() {}

    public static void setActorId(String actorId) {
        ACTOR_ID.set(actorId);
    }

    public static String getActorId() {
        return ACTOR_ID.get();
    }

    public static void clear() {
        ACTOR_ID.remove();
    }
}
