package com.arcane.onslaught.entities.components;

import com.badlogic.ashley.core.Component;

public class XPOrbComponent implements Component {
    public float xpValue;

    public XPOrbComponent(float xpValue) {
        this.xpValue = xpValue;
    }
}
