package powder.client.gui.screens.draggableGui.comp;

import java.util.ArrayList;
import java.util.List;

public class Draggable {

    public static final List<Draggable> draggables = new ArrayList<>();

    public String name;

    public boolean isFocus, isDrag;

    public float lastX, lastY;
    public float x, y, width, height;

    public Draggable(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Draggable byName(String name) {
        for (Draggable draggable : draggables)
            if (draggable.name.equalsIgnoreCase(name))
                return draggable;

        return null;
    }

}
