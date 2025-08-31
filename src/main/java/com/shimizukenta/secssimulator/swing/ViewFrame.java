package com.shimizukenta.secssimulator.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.shimizukenta.secssimulator.SecsSimulatorLog;

public class ViewFrame extends AbstractSwingInternalFrame {
	
	private static final long serialVersionUID = 4744049507304871891L;
	
	private final JLabel communicateStatus;
	private final Color communicateStatusDefaultBgColor;
	
	private final JTextPane messageLogTextArea;
	private final JScrollPane scrollPane;
	private final JScrollBar vScrollBar;
	private final JButton clearButton;

	private enum FilterMode { ALL, EQUIPMENT, HOST }
	private enum Category { EQUIPMENT, HOST, OTHER }

	private volatile FilterMode filterMode = FilterMode.ALL;

	// Match broader subjects to include SECS, SECS1, Block variants
	private static final String SUBJECT_SEND = "Sended SECS";
	private static final String SUBJECT_RECV = "Receive SECS";
	private static final String SUBJECT_TRYSEND = "Try-Send SECS";
	
	public ViewFrame(SwingSecsSimulator parent) {
		super(parent, "Viewer", true, false, true, true);
		
		{
			this.communicateStatus = new JLabel(" ");
			this.communicateStatus.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			this.communicateStatus.setHorizontalAlignment(JLabel.CENTER);
			this.communicateStatus.setVerticalAlignment(JLabel.CENTER);
			this.communicateStatus.setOpaque(false);
			
			this.communicateStatusDefaultBgColor = communicateStatus.getBackground();
			
			final Color fgColor = this.communicateStatus.getForeground();
			
			config().darkMode().addChangeListener(dark -> {
				if ( dark ) {
					this.communicateStatus.setForeground(this.config().defaultDarkAreaForeGroundColor());
				} else {
					this.communicateStatus.setForeground(fgColor);
				}
			});
		}
		{
			this.messageLogTextArea = new JTextPane();
			this.messageLogTextArea.setEditable(false);
		}
		
		this.setLayout(defaultBorderLayout());
		
		{
			// First row: status, filler, clear
			JPanel firstRow = gridPanel(1, 3);
			firstRow.add(communicateStatus);
			firstRow.add(emptyPanel());
			this.clearButton = new JButton("Clear");
			JPanel clearPanel = flowPanel(java.awt.FlowLayout.RIGHT);
			clearPanel.add(clearButton);
			firstRow.add(clearPanel);

			// Second row: filters aligned right
			JRadioButton allBtn = new JRadioButton("All", true);
			JRadioButton equipBtn = new JRadioButton("Equipment");
			JRadioButton hostBtn = new JRadioButton("Host");
			ButtonGroup bg = new ButtonGroup();
			bg.add(allBtn);
			bg.add(equipBtn);
			bg.add(hostBtn);
			JPanel filterPanel = flowPanel(java.awt.FlowLayout.RIGHT);
			filterPanel.add(allBtn);
			filterPanel.add(equipBtn);
			filterPanel.add(hostBtn);

			// Compose header with 2 rows
			JPanel header = new JPanel();
			header.setLayout(defaultBorderLayout());
			header.add(firstRow, BorderLayout.NORTH);
			header.add(filterPanel, BorderLayout.CENTER);

			// Wire filter actions
			allBtn.addActionListener(e -> {
				this.filterMode = FilterMode.ALL;
				refreshTextArea();
			});
			equipBtn.addActionListener(e -> {
				this.filterMode = FilterMode.EQUIPMENT;
				refreshTextArea();
			});
			hostBtn.addActionListener(e -> {
				this.filterMode = FilterMode.HOST;
				refreshTextArea();
			});

			this.add(header, BorderLayout.NORTH);
		}
		{
			this.scrollPane = defaultScrollPane(this.messageLogTextArea);
			this.vScrollBar = this.scrollPane.getVerticalScrollBar();
			
			// Wire clear action after components are ready
			this.clearButton.addActionListener(ev -> {
				synchronized ( this ) {
					this.entries.clear();
					StyledDocument doc = this.messageLogTextArea.getStyledDocument();
					try {
						doc.remove(0, doc.getLength());
					} catch ( BadLocationException ex ) {
						// ignore
					}
				}
				SwingUtilities.invokeLater(() -> {
					this.vScrollBar.setValue(0);
				});
			});
			
			this.add(scrollPane, BorderLayout.CENTER);
		}

		// Initialize text styles for colored categories
		StyledDocument initDoc = this.messageLogTextArea.getStyledDocument();
		Style base = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		this.styleOther = initDoc.addStyle("other", base);
		this.styleEquipment = initDoc.addStyle("equipment", base);
		StyleConstants.setForeground(this.styleEquipment, new Color(0, 128, 0));
		this.styleHost = initDoc.addStyle("host", base);
		StyleConstants.setForeground(this.styleHost, new Color(0, 0, 180));
	}
	
	@Override
	public void setVisible(boolean aFlag) {
		if ( aFlag ) {
			
			int w = this.getDesktopPane().getWidth();
			int h = this.getDesktopPane().getHeight();
			
			this.setBounds(
					(w *  1 / 100),
					(h *  1 / 100),
					(w * 50 / 100),
					(h * 85 / 100));
		}
		
		super.setVisible(aFlag);
		this.moveToBack();
	}
	
	private static final String BR = System.lineSeparator();
	private static final String BRBR = BR + BR;

	private static class LogEntry {
		final String text;
		final Category cat;
		LogEntry(String text, Category cat) {
			this.text = text;
			this.cat = cat;
		}
	}

	private final LinkedList<LogEntry> entries = new LinkedList<>();
	private Style styleEquipment;
	private Style styleHost;
	private Style styleOther;

	private static Category classify(SecsSimulatorLog log) {
		final String subj = log.subject();
		if ( subj.contains(SUBJECT_SEND) || subj.contains(SUBJECT_TRYSEND) ) {
			return Category.EQUIPMENT;
		}
		if ( subj.contains(SUBJECT_RECV) ) {
			return Category.HOST;
		}
		return Category.OTHER;
	}

	private boolean matchesFilter(Category cat) {
		if ( this.filterMode == FilterMode.ALL ) return true;
		if ( cat == Category.OTHER ) return true; // always show non-message logs
		switch ( this.filterMode ) {
		case EQUIPMENT: return cat == Category.EQUIPMENT;
		case HOST: return cat == Category.HOST;
		default: return true;
		}
	}

	private Style styleFor(Category cat) {
		switch ( cat ) {
		case EQUIPMENT: return this.styleEquipment;
		case HOST: return this.styleHost;
		default: return this.styleOther;
		}
	}

	private void refreshTextArea() {
		StyledDocument doc = this.messageLogTextArea.getStyledDocument();
		synchronized ( this ) {
			try {
				doc.remove(0, doc.getLength());
				for ( LogEntry le : this.entries ) {
					if ( matchesFilter(le.cat) ) {
						doc.insertString(doc.getLength(), le.text, styleFor(le.cat));
					}
				}
			} catch ( BadLocationException e ) {
				// ignore
			}
		}
		SwingUtilities.invokeLater(() -> this.vScrollBar.setValue(Integer.MAX_VALUE));
	}

	public void exportViewerLogs(Path path) throws IOException {
		final StringBuilder sb = new StringBuilder();
		synchronized ( this ) {
			for ( LogEntry le : this.entries ) {
				if ( matchesFilter(le.cat) ) {
					sb.append(le.text);
				}
			}
		}
		Files.write(
			path,
			sb.toString().getBytes(StandardCharsets.UTF_8),
			StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE
		);
	}
	
	@Override
	protected void putMessageLog(SecsSimulatorLog log) {
		
			synchronized ( this ) {
			
			String s = log.toString() + BRBR;
			Category cat = classify(log);
			entries.add(new LogEntry(s, cat));
			
			int msgSize = entries.size();
			if ( msgSize > config().viewerSize() ) {
				
				for ( int i = (msgSize / 2); i > 0; --i ) {
					entries.pollFirst();
				}
				
				// After trimming, rebuild based on current filter
				StyledDocument doc = this.messageLogTextArea.getStyledDocument();
				try {
					doc.remove(0, doc.getLength());
					for ( LogEntry le : this.entries ) {
						if ( matchesFilter(le.cat) ) {
							doc.insertString(doc.getLength(), le.text, styleFor(le.cat));
						}
					}
				} catch ( BadLocationException e ) {
					// ignore
				}
				
			} else {
				
				if ( matchesFilter(cat) ) {
					StyledDocument doc = this.messageLogTextArea.getStyledDocument();
					try {
						doc.insertString(doc.getLength(), s, styleFor(cat));
					} catch ( BadLocationException e ) {
						// ignore
					}
				}
			}
		}
		
		SwingUtilities.invokeLater(() -> {
			this.vScrollBar.setValue(Integer.MAX_VALUE);
		});
	}
	
	@Override
	protected void notifyCommunicateStateChanged(boolean communicated) {
		
		if ( communicated ) {
			
			this.communicateStatus.setText("Communicating");
			this.communicateStatus.setBackground(config().communicatingColor());
			this.communicateStatus.setOpaque(true);
			
		} else {
			
			this.communicateStatus.setText("Not communicate");;
			this.communicateStatus.setBackground(this.communicateStatusDefaultBgColor);
			this.communicateStatus.setOpaque(false);
		}
		
		this.communicateStatus.repaint();
	}
	
}
