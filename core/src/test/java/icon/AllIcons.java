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
 *
 */
package icon;

import java.awt.*;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.ListDataListener;

import ui.ComponentDemo;
import util.ClassFinder;
import util.ResourceWalker;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.components.OverlayScrollPane;
import com.github.weisj.darklaf.icons.IconLoader;
import com.github.weisj.darklaf.icons.ThemedSVGIcon;
import com.github.weisj.darklaf.platform.decorations.DecorationsProvider;
import com.github.weisj.darklaf.util.Pair;
import com.kitfox.svg.app.beans.SVGIcon;

public class AllIcons implements ComponentDemo {

    private static final int ICON_SIZE = 50;

    public static void main(final String[] args) {
        ComponentDemo.showDemo(new AllIcons());
    }

    public AllIcons() {
        List<DecorationsProvider> decorationsProviders = ClassFinder.getInstancesOfType(DecorationsProvider.class,
                                                                                        "com.github.weisj");
        LafManager.registerInitTask((currentTheme, defaults) -> {
            Properties props = new Properties();
            decorationsProviders.forEach(provider -> provider.loadDecorationProperties(props, defaults));
            defaults.putAll(props);
        });
    }

    @Override
    public JComponent createComponent() {
        JList<Pair<String, ? extends Icon>> list = new JList<>(new ListModel<Pair<String, ? extends Icon>>() {
            final List<Pair<String, ? extends Icon>> elements = loadIcons();

            @Override
            public int getSize() {
                return elements.size();
            }

            @Override
            public Pair<String, ? extends Icon> getElementAt(final int index) {
                return elements.get(index);
            }

            @Override
            public void addListDataListener(final ListDataListener l) {}

            @Override
            public void removeListDataListener(final ListDataListener l) {}
        });
        list.setLayoutOrientation(JList.VERTICAL);
        list.setCellRenderer(new IconListRenderer());
        return new OverlayScrollPane(list);
    }

    private List<Pair<String, ? extends Icon>> loadIcons() {
        IconLoader loader = IconLoader.get();
        try (ResourceWalker walker = ResourceWalker.walkResources("com.github.weisj")) {
            return walker.stream().parallel()
                         .filter(p -> p.endsWith("svg"))
                         .map(p -> {
                             int size = ICON_SIZE;
                             ThemedSVGIcon icon = (ThemedSVGIcon) loader.loadSVGIcon(p, size, size, true);
                             SVGIcon svgIcon = icon.getSVGIcon();
                             int autosize = svgIcon.getAutosize();
                             svgIcon.setAutosize(SVGIcon.AUTOSIZE_NONE);
                             int width = size;
                             int height = (int) (((double) width / svgIcon.getIconWidth()) * svgIcon.getIconHeight());
                             if (height > size) {
                                 height = size;
                                 width = (int) (((double) height / svgIcon.getIconHeight()) * svgIcon.getIconWidth());
                             }

                             icon.setDisplaySize(width, height);
                             svgIcon.setAutosize(autosize);

                             String name = p.substring(p.lastIndexOf('/') + 1);

                             return new Pair<>(name, new CenterIcon(icon, size, size));
                         })
                         .sorted(Pair.compareFirst())
                         .collect(Collectors.toList());
        }
    }

    @Override
    public String getTitle() {
        return "All Icons";
    }

    private static final class IconListRenderer extends JLabel
                                                implements ListCellRenderer<Pair<String, ? extends Icon>> {

        private IconListRenderer() {
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }

        @Override
        public Component getListCellRendererComponent(final JList<? extends Pair<String, ? extends Icon>> list,
                                                      final Pair<String, ? extends Icon> value, final int index,
                                                      final boolean isSelected, final boolean cellHasFocus) {
            setIcon(value.getSecond());
            setText(value.getFirst());
            return this;
        }
    }

    private static class CenterIcon implements Icon {

        private final Icon icon;
        private final int width;
        private final int height;

        private CenterIcon(final Icon icon, final int width, final int height) {
            this.icon = icon;
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
            int px = x + (width - icon.getIconWidth()) / 2;
            int py = y + (height - icon.getIconHeight()) / 2;
            icon.paintIcon(c, g, px, py);
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
