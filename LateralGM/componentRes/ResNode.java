package componentRes;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

import mainRes.LGM;
import mainRes.Prefs;
import resourcesRes.ResId;

public class ResNode extends DefaultMutableTreeNode implements Transferable
	{
	private static final long serialVersionUID = 1L;
	public static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType,"Node");
	private DataFlavor[] flavors = { NODE_FLAVOR };
	public static byte STATUS_PRIMARY = 1;
	public static byte STATUS_GROUP = 2;
	public static byte STATUS_SECONDARY = 3;
	public byte status;
	public byte kind;
	public ResId resourceId;

	public ResNode(String name, byte status, byte kind, ResId res)
		{
		super(name);
		this.status = status;
		this.kind = kind;
		resourceId = res;
		}

	public ResNode(String name, int status, int kind, ResId res)
		{
		super(name);
		this.status = (byte) status;
		this.kind = (byte) kind;
		resourceId = res;
		}

	public ResNode(String name, int status, int kind)
		{
		super(name);
		this.status = (byte) status;
		this.kind = (byte) kind;
		}

	public ResNode addChild(String name, byte stat, byte type)
		{
		ResNode b = new ResNode(name,stat,type);
		add(b);
		return b;
		}

	public ResNode addChild(String name, int stat, int type)
		{
		return addChild(name,(byte) stat,(byte) type);
		}

	public boolean getAllowsChildren()
		{
		if (Prefs.protectLeaf && status == STATUS_SECONDARY) return false;
		if (Prefs.protectRoot && this == LGM.root) return false;
		return true;
		}

	public DataFlavor[] getTransferDataFlavors()
		{
		return flavors;
		}

	public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		return Arrays.asList(flavors).contains(flavor);
		}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
		if (flavor != NODE_FLAVOR) throw new UnsupportedFlavorException(flavor);
		return this;
		}
	}