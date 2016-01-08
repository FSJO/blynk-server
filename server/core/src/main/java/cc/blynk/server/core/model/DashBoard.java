package cc.blynk.server.core.model;

import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.widgets.MultiPinWidget;
import cc.blynk.server.core.model.widgets.OnePinWidget;
import cc.blynk.server.core.model.widgets.Widget;
import cc.blynk.server.core.model.widgets.outputs.FrequencyWidget;
import cc.blynk.server.core.protocol.exceptions.IllegalCommandBodyException;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.utils.JsonParser;
import cc.blynk.utils.StringUtils;

import java.util.Arrays;

/**
 * User: ddumanskiy
 * Date: 21.11.13
 * Time: 13:04
 */
public class DashBoard {

    public int id;

    public String name;

    public long createdAt;

    public long updatedAt;

    public Widget[] widgets = {};

    public String boardType;

    public String theme;

    public boolean keepScreenOn;

    public boolean isShared;

    public boolean isActive;

    /**
     * Specific property used for improving user experience on mobile application.
     * In case user activated dashboard before hardware connected to server, user have to
     * deactivate and activate dashboard again in order to setup PIN MODES (OUT, IN).
     * With this property problem resolved by server side. Command for setting Pin Modes
     * is remembered and when hardware goes online - server sends Pin Modes command to hardware
     * without requiring user to activate/deactivate dashboard again.
     */
    public transient StringMessage pinModeMessage;

    private static void append(StringBuilder sb, Byte pin, PinType pinType, String pinMode) {
        if (pin == null || pin == -1 || pinMode == null || pinType == PinType.VIRTUAL) {
            return;
        }
        sb.append(StringUtils.BODY_SEPARATOR)
                .append(pin)
                .append(StringUtils.BODY_SEPARATOR)
                .append(pinMode);
    }

    public void update(HardwareBody hardwareBody) {
        for (Widget widget : widgets) {
            widget.updateIfSame(hardwareBody);
        }
        this.updatedAt = System.currentTimeMillis();
    }

    public FrequencyWidget findReadingWidget(String body, int msgId) {
        final HardwareBody hardwareBody = new HardwareBody(body, msgId);
        for (Widget widget : widgets) {
            if (widget instanceof FrequencyWidget && widget.isSame(hardwareBody.pin, hardwareBody.type)) {
                return (FrequencyWidget) widget;
            }
        }
        throw new IllegalCommandBodyException("No frequency widget for read command.", msgId);
    }

    public Widget findWidgetByPin(byte pin, PinType pinType) {
        for (Widget widget : widgets) {
            if (widget.isSame(pin, pinType)) {
                return widget;
            }
        }
        return null;
    }

    public  <T> T getWidgetByType(Class<T> clazz) {
        for (Widget widget : widgets) {
            if (clazz.isInstance(widget)) {
                return clazz.cast(widget);
            }
        }
        return null;
    }

    public String buildPMMessage() {
        StringBuilder sb = new StringBuilder("pm");
        for (Widget widget : widgets) {
            if (widget instanceof OnePinWidget) {
                OnePinWidget onePinWidget = (OnePinWidget) widget;
                append(sb, onePinWidget.pin, onePinWidget.pinType, onePinWidget.getModeType());
            } else if (widget instanceof MultiPinWidget) {
                MultiPinWidget multiPinWidget = (MultiPinWidget) widget;
                for (Pin pin : multiPinWidget.pins) {
                    append(sb, pin.pin, pin.pinType, multiPinWidget.getModeType());
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashBoard dashBoard = (DashBoard) o;

        if (id != dashBoard.id) return false;
        if (name != null ? !name.equals(dashBoard.name) : dashBoard.name != null) return false;
        if (!Arrays.equals(widgets, dashBoard.widgets)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (widgets != null ? Arrays.hashCode(widgets) : 0);
        return result;
    }

    @Override
    public String toString() {
        return JsonParser.toJson(this);
    }
}