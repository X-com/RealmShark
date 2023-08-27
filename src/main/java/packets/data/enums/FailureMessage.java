package packets.data.enums;

import java.io.Serializable;

/**
 * Common failure packet messages for when the error code is 0
 */
public enum FailureMessage implements Serializable {
    CharacterDead("Character is dead"),
    CharacterNotFound("Character not found"),
    TemporaryBan("Your IP has been temporarily banned for abuse/hacking on this server");

    String message;

    FailureMessage(String s) {
        message = s;
    }
}
