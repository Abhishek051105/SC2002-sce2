
package entity;

/**
 * Customer types used only to describe the arrival list in GameConfig.
 * Game logic never switches on this; each Customer subclass handles its
 * own behaviour.
 *
 * @author Aaryan
 * @since 2026-11-01 (Stage 2)
 */
public enum CustomerType {
    REGULAR, VIP, CRITIC
}
