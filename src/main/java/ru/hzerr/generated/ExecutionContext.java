package ru.hzerr.generated;

import java.util.StringJoiner;

public class ExecutionContext {

    private String targetId;
    private String type;
    private String title;
    private String url;
    private boolean attached;
    private String openerId;
    private boolean canAccessOpener;
    private String openerFrameId;
    private String parentFrameId;
    private String browserContextId;
    private String subtype;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAttached() {
        return attached;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    public String getOpenerId() {
        return openerId;
    }

    public void setOpenerId(String openerId) {
        this.openerId = openerId;
    }

    public boolean isCanAccessOpener() {
        return canAccessOpener;
    }

    public void setCanAccessOpener(boolean canAccessOpener) {
        this.canAccessOpener = canAccessOpener;
    }

    public String getOpenerFrameId() {
        return openerFrameId;
    }

    public void setOpenerFrameId(String openerFrameId) {
        this.openerFrameId = openerFrameId;
    }

    public String getParentFrameId() {
        return parentFrameId;
    }

    public void setParentFrameId(String parentFrameId) {
        this.parentFrameId = parentFrameId;
    }

    public String getBrowserContextId() {
        return browserContextId;
    }

    public void setBrowserContextId(String browserContextId) {
        this.browserContextId = browserContextId;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ExecutionContext.class.getSimpleName() + "[", "]")
                .add("targetId='" + targetId + "'")
                .add("type='" + type + "'")
                .add("title='" + title + "'")
                .add("url='" + url + "'")
                .add("attached=" + attached)
                .add("openerId='" + openerId + "'")
                .add("canAccessOpener=" + canAccessOpener)
                .add("openerFrameId='" + openerFrameId + "'")
                .add("parentFrameId='" + parentFrameId + "'")
                .add("browserContextId='" + browserContextId + "'")
                .add("subtype='" + subtype + "'")
                .toString();
    }
}
