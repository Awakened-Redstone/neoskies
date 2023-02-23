package skylands.api.island;

import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;

public enum PermissionLevel {
    OWNER(99),
    MEMBER(5),
    VISITOR(0);

    private final byte level;

    @Contract(pure = true)
    PermissionLevel(int level) {
        this.level = MathHelper.clamp((byte) level, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public byte getLevel() {
        return level;
    }

    public static PermissionLevel fromValue(int value) {
        for (PermissionLevel permissionLevel : PermissionLevel.values()) {
            if (permissionLevel.level == value) return permissionLevel;
        }

        return null;
    }
}
