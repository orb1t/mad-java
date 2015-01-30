/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.swing;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;


/**
 * A simpler alternative to a JPanel with a CardLayout.  The AWT CardLayout
 * layout manager can be inconvenient to use because the special "stack of
 * cards" operations it supports require a cast to use.  For example to show
 * the card named "myCard" given a JPanel with a CardLayout one would write:
 * <pre>
 * ((CardLayout)(myJPanel.getLayout())).show(myJPanel, "myCard");
 * </pre>
 * This doesn't work well with Swing - all of the CardLayout display operations,
 * like <code>show</code> call validate directly.  Swing supports automatic
 * validation (see JComponent.revalidate()); this direct call to validate is
 * inefficient.
 * <p>
 * The CardPane JPanel subclass is intended to support a layout with a modest number
 * of cards, on the order of 100 or less.  A cards name is it's component
 * name, as in java.awt.Component.getName(), which is set when the component
 * is added to the CardPanel:
 * <pre>
 * myCardPanel.add(myChild, "MyChildName");
 * myChild.getName() <i>=&gt; "MyChildName"</i>
 * </pre>
 * As with CardLayout, the first child added to a CardPanel is made visible
 * and there's only one child visible at a time.  The <code>showCard</code>
 * method accepts either a childs name or the child itself:
 * <pre>
 * myCardPanel.show("MyChildName");
 * myCardPanel.show(myChild);
 * </pre>
 * <p>
 * The CardPanel class doesn't support the vgap/hgap CardLayout properties since
 * one can add a Border, see JComponent.setBorder().
 */

public class CardPanel extends JPanel
{
	private static final long serialVersionUID = -7155388611045482798L;


	private static class Layout implements LayoutManager
    {
	/**
	 * Set the childs name (if non-null) and and make it visible
	 * iff it's the only CardPanel child.
	 * @see java.awt.Component#setName
	 */
	@Override
	public void addLayoutComponent(final String name, final Component child) {
	    if (name != null) {
		child.setName(name);
	    }
	    child.setVisible(child.getParent().getComponentCount() == 1);
	}

	/**
	 * If this child was visible, then make the first remaining
	 * child visible.
	 */
	@Override
	public void removeLayoutComponent(final Component child) {
	    if (child.isVisible()) {
		final Container parent = child.getParent();
		if (parent.getComponentCount() > 0) {
		    parent.getComponent(0).setVisible(true);
		}
	    }
	}

	/**
	 * @return the maximum preferred width/height + the parents insets
	 */
	@Override
	public Dimension preferredLayoutSize(final Container parent) {
	    final int nChildren = parent.getComponentCount();
	    final Insets insets = parent.getInsets();
	    int width = insets.left + insets.right;
	    int height = insets.top + insets.bottom;

	    for (int i = 0; i < nChildren; i++) {
		final Dimension d = parent.getComponent(i).getPreferredSize();
		if (d.width > width) {
		    width = d.width;
		}
		if (d.height > height) {
		    height = d.height;
		}
	    }
	    return new Dimension(width, height);
	}

	/**
	 * @return the maximum minimum width/height + the parents insets
	 */
	@Override
	public Dimension minimumLayoutSize(final Container parent) {
	    final int nChildren = parent.getComponentCount();
	    final Insets insets = parent.getInsets();
	    int width = insets.left + insets.right;
	    int height = insets.top + insets.bottom;

	    for (int i = 0; i < nChildren; i++) {
		final Dimension d = parent.getComponent(i).getMinimumSize();
		if (d.width > width) {
		    width = d.width;
		}
		if (d.height > height) {
		    height = d.height;
		}
	    }
	    return new Dimension(width, height);
	}

	@Override
	public void layoutContainer(final Container parent) {
	    final int nChildren = parent.getComponentCount();
	    final Insets insets = parent.getInsets();
	    for (int i = 0; i < nChildren; i++) {
		final Component child = parent.getComponent(i);
		if (child.isVisible()) {
		    final Rectangle r = parent.getBounds();
		    final int width = r.width - insets.left + insets.right;
		    final int height = r.height - insets.top + insets.bottom;
		    child.setBounds(insets.left, insets.top, width, height);
		    break;
		}
	    }
	}
    }


    /**
     * Creates a CardPanel.  Children, called "cards" in this API, should be added
     * with add().  The first child we be made visible, subsequent children will
     * be hidden.  To show a card, use one of the show*Card methods.
     */
    public CardPanel() {
	super(new Layout());
    }


    /**
     * Return the index of the first (and one would hope - only)
     * visible child.  If a visible child can't be found,
     * perhaps the caller has inexlicably hidden all of the
     * children, then return -1.
     */
    private int getVisibleChildIndex() {
	final int nChildren = getComponentCount();
	for (int i = 0; i < nChildren; i++) {
	    final Component child = getComponent(i);
	    if (child.isVisible()) {
		return i;
	    }
	}
	return -1;
    }


    /**
     * Hide the currently visible child  "card" and show the
     * specified card.  If the specified card isn't a child
     * of the CardPanel then we add it here.
     */
    public void showCard(final Component card) {
	if (card.getParent() != this) {
	    add(card);
	}
	final int index = getVisibleChildIndex();
	if (index != -1) {
	    getComponent(index).setVisible(false);
	}
	card.setVisible(true);
	revalidate();
	repaint();
    }


    /**
     * Show the card with the specified name.
     * @see java.awt.Component#getName
     */
    public void showCard(final String name) {
	final int nChildren = getComponentCount();
	for (int i = 0; i < nChildren; i++) {
	    final Component child = getComponent(i);
	    if (child.getName().equals(name)) {
		showCard(child);
		break;
	    }
	}
    }


    /**
     * Show the card that was added to this CardPanel after the currently
     * visible card.  If the currently visible card was added last, then
     * show the first card.
     */
    public void showNextCard() {
	if (getComponentCount() <= 0) {
	    return;
	}
	final int index = getVisibleChildIndex();
	if (index == -1) {
	    showCard(getComponent(0));
	}
	else if (index == (getComponentCount() - 1)) {
	    showCard(getComponent(0));
	}
	else {
	    showCard(getComponent(index + 1));
	}
    }


    /**
     * Show the card that was added to this CardPanel before the currently
     * visible card.  If the currently visible card was added first, then
     * show the last card.
     */
    public void showPreviousCard() {
	if (getComponentCount() <= 0) {
	    return;
	}
	final int index = getVisibleChildIndex();
	if (index == -1) {
	    showCard(getComponent(0));
	}
	else if (index == 0) {
	    showCard(getComponent(getComponentCount() - 1));
	}
	else {
	    showCard(getComponent(index -1));
	}
    }


    /**
     * Show the first card that was added to this CardPanel.
     */
    public void showFirstCard() {
	if (getComponentCount() <= 0) {
	    return;
	}
	showCard(getComponent(0));
    }


    /**
     * Show the last card that was added to this CardPanel.
     */
    public void showLastCard() {
	if (getComponentCount() <= 0) {
	    return;
	}
	showCard(getComponent(getComponentCount() - 1));
    }
}

