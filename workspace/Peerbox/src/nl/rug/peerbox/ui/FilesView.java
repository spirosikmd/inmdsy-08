package nl.rug.peerbox.ui;

import nl.rug.peerbox.logic.Peerbox;
import nl.rug.peerbox.logic.PeerboxFile;
import nl.rug.peerbox.logic.VFSListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class FilesView extends Composite implements DisposeListener,
		VFSListener, SelectionListener {

	private final Font title;
	private final Color foreground;
	private final Peerbox peerbox;
	private final Text text;

	public FilesView(Composite c, Peerbox peerbox) {
		super(c, SWT.NONE);
		this.peerbox = peerbox;
		this.peerbox.getVirtualFilesystem().addVFSListener(this);

		Display display = Display.getCurrent();
		title = new Font(display, "Arial", 13, SWT.NORMAL);
		foreground = new Color(display, 75, 75, 75);
		setFont(title);
		setForeground(foreground);
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawText("Shared Files", 20, 15);
				e.gc.dispose();
			}
		});

		GridLayout layout = new GridLayout();
		layout.marginTop = 40;
		layout.marginLeft = 20;
		layout.numColumns = 1;
		setLayout(layout);

		Button request = new Button(this, SWT.PUSH);
		request.setText("Request");
		request.addSelectionListener(this);
		
		text = new Text(this, SWT.NONE);
		text.setText("");
		GridData textData = new GridData();
		textData.grabExcessVerticalSpace=true;
		textData.verticalAlignment=GridData.FILL;
		textData.grabExcessHorizontalSpace=true;
		textData.horizontalAlignment=GridData.FILL;
		text.setLayoutData(textData);
	}

	@Override
	public void widgetDisposed(DisposeEvent de) {
		title.dispose();
		foreground.dispose();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent se) {
	}

	@Override
	public void widgetSelected(SelectionEvent se) {
		peerbox.requestFiles();
	}

	@Override
	public void updated() {
		
		this.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				StringBuffer fileText = new StringBuffer();
				for (PeerboxFile f : peerbox.getVirtualFilesystem().getFileList()) {
					fileText.append(f.getFilename());
					fileText.append(" @");
					fileText.append(f.getOwner().getName());
					fileText.append(System.lineSeparator());
				}
				text.setText(fileText.toString());
			}
		});
		
		
	}

}
