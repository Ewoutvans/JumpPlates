package cloud.zeroprox.jumpplate;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;

import java.util.Optional;

@Plugin(
        id = "jumpplate",
        name = "JumpPlate",
        description = "JumpPlate plugin",
        url = "https://zeroprox.cloud",
        authors = {
                "ewoutvs_",
                "Alagild"
        }
)
public class JumpPlate {

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        PermissionService service = Sponge.getServiceManager().provide(PermissionService.class).get();
        PermissionDescription.Builder builder = service.newDescriptionBuilder(this);
        builder.id("jumpplates.create")
                .description(Text.of("Allows a user to create a [JumpPlate] sign."))
                .assign(PermissionDescription.ROLE_ADMIN, true)
                .register();
    }

    @Listener
    public void onMove(MoveEntityEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();
            Location feet = event.getToTransform().getLocation().add(0, 0, 0);
            if (feet.getBlockType().equals(BlockTypes.STONE_PRESSURE_PLATE)) {
                Location signLocation = feet.add(0, -2, 0);
                if (signLocation.getTileEntity().isPresent()) {
                    TileEntity tileEntity = (TileEntity) signLocation.getTileEntity().get();
                    if (tileEntity.getType().equals(TileEntityTypes.SIGN)) {
                        Sign sign = (Sign) tileEntity;
                        // Filter non ASCII chars in title because Apple adds some stupid down arrow unicode things
                        if (sign.lines().get(0).toPlain().replaceAll("[^\\x20-\\x7e]", "").equalsIgnoreCase("[JumpPlate]")) {
                            player.setVelocity(player.getVelocity().mul(Double.valueOf(sign.lines().get(1).toPlainSingle().replaceAll("[^\\d.]", ""))).add(0, Double.valueOf(sign.lines().get(2).toPlainSingle().replaceAll("[^\\d.]", "")), 0));
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onSign(ChangeSignEvent event) {
        if (event.getText().lines().get(0).toPlain().equalsIgnoreCase("[JumpPlate]")) {
            System.out.println(event.getCause());
            Optional<Player> player = event.getCause().first(Player.class);
            if (player.isPresent()) {
                if (player.get().hasPermission("jumpplates.create")) {
                    System.out.println(event.getText().lines().get(1).toPlain().replaceAll("[^\\d.]", ""));
                    if (event.getText().lines().get(1).toPlain().replaceAll("[^\\d.]", "").length() < 1) {
                        player.get().sendMessage(Text.of(TextColors.RED, "Line 2 needs the strength (number)"));
                        return;
                    }
                    if (event.getText().lines().get(2).toPlain().replaceAll("[^\\d.]", "").length() < 1) {
                        player.get().sendMessage(Text.of(TextColors.RED, "Line 3 needs a height (number)"));
                        return;
                    }
                    player.get().sendMessage(Text.of(TextColors.GREEN, "You created a jump sign."));
                } else {
                    player.get().sendMessage(Text.of(TextColors.RED, "You are not allowed to make a jump sign."));
                    event.setCancelled(true);
                }
            }
        }
    }
}
