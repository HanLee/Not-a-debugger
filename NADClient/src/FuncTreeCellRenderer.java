import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

class FuncTreeCellRenderer extends DefaultTreeCellRenderer{

	private static final long serialVersionUID = 1L;


    public Component getTreeCellRendererComponent(JTree tree,
        Object value, boolean selected, boolean expanded,
        boolean leaf, int row, boolean hasFocus){

        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        MethodStatusObject mso = (MethodStatusObject) node.getUserObject();
        
        if(mso.getHooked())
        {
        	setBackground(Color.gray);
        	setOpaque(true);
        }
        else
        {
        	setBackground(Color.white);
        	setOpaque(false);
        }

        return this;
    }
}
