package nl.rug.peerbox.ui;

import nl.rug.peerbox.logic.Peer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class PeerView extends Composite implements DisposeListener {

	private int hostID;
	private Peer model;
	private final Text peerID;
	private final Text owner;
	private final Text address;
	private final Color background;
	private final Color foreground;

	public PeerView(Composite parent) {
		super(parent, SWT.BORDER);
		addDisposeListener(this);

		background = new Color(getDisplay(), new RGB(215, 215, 215));
		foreground = new Color(getDisplay(), new RGB(75, 75, 75));
		setBackground(background);
		setForeground(foreground);
		GridLayout layout = new GridLayout();
		layout.numColumns = 12;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		setLayout(layout);

		GridData ownerData = new GridData();
		ownerData.grabExcessHorizontalSpace = true;
		ownerData.horizontalAlignment = SWT.FILL;
		ownerData.horizontalSpan = 4;
		owner = new Text(this, SWT.NONE);
		owner.setBackground(background);
		owner.setForeground(foreground);
		owner.setLayoutData(ownerData);

		GridData addressData = new GridData();
		addressData.grabExcessHorizontalSpace = true;
		addressData.horizontalAlignment = SWT.FILL;
		addressData.horizontalSpan = 4;
		address = new Text(this, SWT.NONE);
		address.setBackground(background);
		address.setForeground(foreground);
		address.setLayoutData(ownerData);

		GridData peerIDData = new GridData();
		peerIDData.grabExcessHorizontalSpace = true;
		peerIDData.horizontalAlignment = SWT.FILL;
		peerIDData.horizontalSpan = 4;
		peerID = new Text(this, SWT.NONE);
		peerID.setBackground(background);
		peerID.setForeground(foreground);
		peerID.setLayoutData(peerIDData);
		peerID.setEditable(false);

	}

	public void setHostID(int hostID) {
		this.hostID = hostID;
		peerID.setText(hostID + "");
	}

	public int getHostID() {
		return hostID;
	}

	@Override
	public Point computeSize(int whint, int hhint, boolean changed) {
		super.computeSize(whint, hhint, changed);
		Point size = new Point(0, 0);
		if (whint == SWT.DEFAULT) {
			size.x = 150;
		} else {
			size.x = (whint < 50) ? 50 : whint;
		}
		size.y = 30;
		return size;
	}

	@Override
	public void widgetDisposed(DisposeEvent arg0) {
		background.dispose();
		foreground.dispose();
	}

	public Peer getModel() {
		return model;
	}

	public void setModel(Peer model) {
		this.model = model;
		if (model != null) {
			owner.setText(model.getName());
			address.setText(model.getAddress().toString() + ":"
					+ model.getPort());
		}
	}

	// @Override
	// public void modelUpdated() {
	// this.getDisplay().asyncExec(new Runnable() {
	//
	// @Override
	// public void run() {
	// if (getModel().exists()) {
	// action.setVisible(false);
	// }
	// layout();
	// }
	// });
	// }

}
