package nl.rug.peerbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class LogView extends Composite implements DisposeListener {

	private final Font title;
	private final Color foreground;

	public LogView(Composite c) {
		super(c, SWT.NONE);
		Display display = Display.getCurrent();
		title = new Font(display,"Arial", 13, SWT.NORMAL );
		foreground = new Color(display, 75,75,75);
		setFont(title);
		setForeground(foreground);
		addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawText("Log File", 20, 15);
				e.gc.dispose();
			}
		});
	}

	@Override
	public void widgetDisposed(DisposeEvent arg0) {
		title.dispose();
		foreground.dispose();
	}
	
}
