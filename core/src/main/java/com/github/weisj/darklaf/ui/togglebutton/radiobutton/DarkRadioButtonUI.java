/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.weisj.darklaf.ui.togglebutton.radiobutton;

import com.github.weisj.darklaf.icons.EmptyIcon;
import com.github.weisj.darklaf.ui.togglebutton.DarkToggleButtonKeyHandler;
import com.github.weisj.darklaf.ui.togglebutton.DarkToggleButtonUI;
import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonConstants;
import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonFocusNavigationActions;
import com.github.weisj.darklaf.util.GraphicsContext;
import com.github.weisj.darklaf.util.GraphicsUtil;
import com.github.weisj.darklaf.util.PropertyKey;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.metal.MetalRadioButtonUI;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Konstantin Bulenkov
 * @author Jannis Weis
 */
public class DarkRadioButtonUI extends MetalRadioButtonUI implements PropertyChangeListener, ToggleButtonConstants {

    protected static final Rectangle viewRect = new Rectangle();
    protected static final Rectangle iconRect = new Rectangle();
    protected static final Rectangle textRect = new Rectangle();
    protected static Dimension size = new Dimension();
    protected RectangularShape hitArea;
    protected JToggleButton radioButton;
    protected int iconBaselineOffset;

    private Icon stateIcon;
    protected BasicButtonListener buttonListener;
    protected KeyListener keyListener;


    public static ComponentUI createUI(final JComponent c) {
        return new DarkRadioButtonUI();
    }

    @Override
    public void installUI(final JComponent c) {
        radioButton = (JToggleButton) c;
        super.installUI(c);
    }

    @Override
    public void installDefaults(final AbstractButton b) {
        super.installDefaults(b);
        LookAndFeel.installProperty(b, PropertyKey.OPAQUE, false);
        iconBaselineOffset = UIManager.getInt(getPropertyPrefix() + "iconBaselineOffset");
        hitArea = new Rectangle();
        installIcons();
    }


    protected void installIcons() {
        stateIcon = UIManager.getIcon("RadioButton.icon");
    }

    @Override
    protected void installListeners(final AbstractButton button) {
        buttonListener = createButtonListener(button);
        button.addMouseListener(buttonListener);
        button.addMouseMotionListener(buttonListener);
        button.addFocusListener(buttonListener);
        button.addPropertyChangeListener(buttonListener);
        button.addChangeListener(buttonListener);
        keyListener = createKeyListener(button);
        button.addKeyListener(keyListener);
        ToggleButtonFocusNavigationActions.installActions(radioButton);
        button.addPropertyChangeListener(this);
    }

    protected KeyListener createKeyListener(final AbstractButton button) {
        return new DarkToggleButtonKeyHandler();
    }

    @Override
    public void uninstallUI(final JComponent c) {
        super.uninstallUI(c);
        radioButton = null;
    }

    @Override
    protected void uninstallListeners(final AbstractButton button) {
        button.removeMouseListener(buttonListener);
        button.removeMouseMotionListener(buttonListener);
        button.removeFocusListener(buttonListener);
        button.removeChangeListener(buttonListener);
        button.removePropertyChangeListener(buttonListener);
        buttonListener = null;
        button.removeKeyListener(keyListener);
        keyListener = null;
        ToggleButtonFocusNavigationActions.uninstallActions(radioButton);
        button.removePropertyChangeListener(this);
    }

    @Override
    public void paint(final Graphics g2d, final JComponent c) {
        Graphics2D g = (Graphics2D) g2d;
        AbstractButton b = (AbstractButton) c;

        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = SwingUtilities2.getFontMetrics(c, g, f);

        String text = layout(b, fm);

        paintBackground(c, g);
        GraphicsContext config = GraphicsUtil.setupStrokePainting(g);
        Icon icon = getIconBullet(c, g, b);
        if (icon != null) {
            icon.paintIcon(c, g, iconRect.x, iconRect.y);
        } else {
            Icon radioIcon = getStateIcon(b);
            if (radioIcon != null) {
                radioIcon.paintIcon(c, g, iconRect.x, iconRect.y);
            }
        }
        config.restore();
        if (text != null) {
            paintText(g, b, textRect, text, fm, getDisabledTextColor());
        }
    }

    protected void paintBackground(final JComponent c, final Graphics2D g) {
        if (c.isOpaque()) {
            g.setColor(c.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
    }

    public static void paintText(final Graphics2D g, final AbstractButton b,
                                 final Rectangle textRect, final String text, final FontMetrics fm,
                                 final Color disabledTextColor) {
        GraphicsContext context = GraphicsUtil.setupAntialiasing(g);
        g.setFont(b.getFont());
        View view = (View) b.getClientProperty(BasicHTML.propertyKey);
        if (view != null) {
            view.paint(g, textRect);
        } else {
            g.setColor(b.isEnabled() ? b.getForeground() : disabledTextColor);
            SwingUtilities2.drawStringUnderlineCharAt(b, g, text,
                                                      b.getDisplayedMnemonicIndex(),
                                                      textRect.x,
                                                      textRect.y + fm.getAscent());
        }
        context.restore();
    }

    protected Icon getStateIcon(final AbstractButton b) {
        return stateIcon;
    }

    public static Icon getIconBullet(final JComponent c, final Graphics2D g, final AbstractButton b) {
        ButtonModel model = b.getModel();
        Icon icon = b.getIcon();
        if (!model.isEnabled()) {
            if (model.isSelected()) {
                icon = b.getDisabledSelectedIcon();
            } else {
                icon = b.getDisabledIcon();
            }
        } else if (model.isPressed() && model.isArmed()) {
            icon = b.getPressedIcon();
            if (icon == null) {
                // Use selected icon
                icon = b.getSelectedIcon();
            }
        } else if (model.isSelected()) {
            if (b.isRolloverEnabled() && model.isRollover()) {
                icon = b.getRolloverSelectedIcon();
                if (icon == null) {
                    icon = b.getSelectedIcon();
                }
            } else {
                icon = b.getSelectedIcon();
            }
        } else if (b.isRolloverEnabled() && model.isRollover()) {
            icon = b.getRolloverIcon();
        }

        if (icon == null) {
            icon = b.getIcon();
        }
        return icon;
    }

    @Override
    public Icon getDefaultIcon() {
        return new IconUIResource(EmptyIcon.create(20));
    }

    protected String layout(final AbstractButton b, final FontMetrics fm) {
        Insets i = b.getInsets();
        size = b.getSize(size);
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = size.width - (i.right + viewRect.x);
        viewRect.height = size.height - (i.bottom + viewRect.y);
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.x = textRect.y = textRect.width = textRect.height = 0;

        String text = SwingUtilities.layoutCompoundLabel(b, fm, b.getText(), getDefaultIcon(),
                                                         b.getVerticalAlignment(), b.getHorizontalAlignment(),
                                                         b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                                                         viewRect, iconRect, textRect, b.getIconTextGap());
        iconRect.y += iconBaselineOffset;
        if (ToggleButtonConstants.isTreeOrTableCellEditor(b)) {
            hitArea = calculateHitArea();
        } else {
            int x = Math.min(iconRect.x, textRect.x);
            int y = Math.min(iconRect.y, textRect.y);
            int xEnd = Math.max(iconRect.x + iconRect.width, textRect.x + textRect.width);
            int yEnd = Math.max(iconRect.y + iconRect.height, textRect.y + textRect.y);
            hitArea = new Rectangle(x, y, xEnd - x, yEnd - y);
        }
        return text;
    }

    protected RectangularShape calculateHitArea() {
        return new Ellipse2D.Float(Math.max(iconRect.x, 0), Math.max(iconRect.y, 0),
                                   iconRect.width, iconRect.height);
    }

    @Override
    public boolean contains(final JComponent c, final int x, final int y) {
        if (hitArea == null || (hitArea.isEmpty() && c instanceof JRadioButton)) {
            layout((JRadioButton) c, c.getFontMetrics(c.getFont()));
        }
        return hitArea.contains(x, y);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String key = evt.getPropertyName();
        if (PropertyKey.COMPONENT_ORIENTATION.equals(key)) {
            radioButton.repaint();
            hitArea.setFrame(0, 0, 0, 0);
        } else if (DarkToggleButtonUI.KEY_IS_TREE_EDITOR.equals(key)
                   || DarkToggleButtonUI.KEY_IS_TABLE_EDITOR.equals(key)) {
            radioButton.repaint();
            hitArea.setFrame(0, 0, 0, 0);
        } else if (DarkToggleButtonUI.KEY_CLEAR_HIT_AREA.equals(key)) {
            hitArea.setFrame(0, 0, 0, 0);
        }
    }
}