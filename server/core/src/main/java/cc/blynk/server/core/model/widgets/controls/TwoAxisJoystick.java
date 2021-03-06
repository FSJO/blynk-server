package cc.blynk.server.core.model.widgets.controls;

import cc.blynk.server.core.model.Pin;
import cc.blynk.server.core.model.widgets.MultiPinWidget;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.StringJoiner;

import static cc.blynk.server.core.protocol.enums.Command.HARDWARE;
import static cc.blynk.server.core.protocol.enums.Command.SYNC;
import static cc.blynk.utils.BlynkByteBufUtil.makeUTF8StringMessage;
import static cc.blynk.utils.StringUtils.BODY_SEPARATOR_STRING;
import static cc.blynk.utils.StringUtils.makeBody;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 21.03.15.
 */
public class TwoAxisJoystick extends MultiPinWidget implements HardwareSyncWidget {

    public boolean split;

    public boolean autoReturnOn;

    public boolean portraitLocked;

    @Override
    public void send(ChannelHandlerContext ctx, int msgId) {
        if (pins == null) {
            return;
        }
        if (split) {
            for (Pin pin : pins) {
                if (pin.notEmpty()) {
                    ctx.write(makeUTF8StringMessage(HARDWARE, msgId, pin.makeHardwareBody()), ctx.voidPromise());
                }
            }
        } else {
            if (pins[0].notEmpty()) {
                ctx.write(makeUTF8StringMessage(HARDWARE, msgId, pins[0].makeHardwareBody()), ctx.voidPromise());
            }
        }
    }

    @Override
    public void sendSyncOnActivate(Channel appChannel, int dashId) {
        if (pins == null) {
            return;
        }
        if (split) {
            for (Pin pin : pins) {
                if (pin.notEmpty()) {
                    String body = makeBody(dashId, deviceId, pin.makeHardwareBody());
                    appChannel.write(makeUTF8StringMessage(SYNC, 1111, body), appChannel.voidPromise());
                }
            }
        } else {
            if (pins[0].notEmpty()) {
                String body = makeBody(dashId, deviceId, pins[0].makeHardwareBody());
                appChannel.write(makeUTF8StringMessage(SYNC, 1111, body), appChannel.voidPromise());
            }
        }
    }

    @Override
    public String getJsonValue() {
        if (pins == null) {
            return "[]";
        }

        if (isSplitMode()) {
            return super.getJsonValue();
        } else {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            if (pins[0].notEmpty()) {
                for (String pinValue : pins[0].value.split(BODY_SEPARATOR_STRING)) {
                    sj.add("\"" + pinValue + "\"");
                }
            }
            return sj.toString();
        }
    }

    public boolean isSplitMode() {
        return split;
    }

    @Override
    public String getModeType() {
        return "out";
    }


    @Override
    public int getPrice() {
        return 400;
    }
}
