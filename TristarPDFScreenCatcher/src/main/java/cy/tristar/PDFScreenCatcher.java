package cy.tristar;

import java.awt.AWTException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

public class PDFScreenCatcher implements ActionListener {

	private static final String SUCCESS = "PDF is ready!";
	private static final String ERROR = "There was a problem with PDF Screen Catcher :(";
	
	private String loc;
	private String pdfName;
	private JFrame menu;
	private JLabel msg;
	private JTextField tfield;
	private String imageloc;
	
	public PDFScreenCatcher(String loc, String defPdfName) {
		this.loc = loc;
		this.pdfName = defPdfName;
		this.imageloc = this.loc + File.separator + "temp.jpg";
		
		this.menu = new JFrame("Tristar PDF Screen Catcher");
		this.populateMenu();
	}
	
	private void populateMenu() {
		menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menu.setLocationRelativeTo(null);
		menu.setAlwaysOnTop(true);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(new EmptyBorder(20, 20, 20, 20));
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel logolabel = new JLabel("Tristar PDF Screen Catcher", JLabel.CENTER);
		logolabel.setFont(logolabel.getFont().deriveFont(22.0f));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(logolabel, c);
		
		JLabel urllabel = new JLabel("www.tristarcy.com", JLabel.CENTER);
		urllabel.setFont(urllabel.getFont().deriveFont(18.0f));
		urllabel.setBorder(new EmptyBorder(5, 0, 20, 0));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		panel.add(urllabel, c);
		
		c.gridwidth= 1;
		
		JLabel label = new JLabel("PDF name   ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		panel.add(label, c);

		this.tfield = new JTextField(20);
		tfield.setText(this.pdfName + ".pdf ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		panel.add(tfield,c);

		JButton button = new JButton();
		button.setText("Capture Screen");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 3;
		panel.add(button, c);
        button.addActionListener(this);

		JLabel emplabel = new JLabel(" ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth= 2;
		c.gridx = 0;
		c.gridy = 4;
		panel.add(emplabel, c);
		
		this.msg = new JLabel(" ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 5;
		panel.add(msg, c);

		
		menu.add(panel);


		menu.pack();
		menu.setVisible(true);
	}
	

	public void actionPerformed(ActionEvent arg0) {
		this.menu.setVisible(false);
		boolean r1 = this.imageMaker();
		if (r1) {
			boolean r2 = this.pdfMaker();
			if (r2)
				this.msg.setText("<html>" + SUCCESS + "<br /> you can find your pdf at " + (this.loc + File.separator) + "</html>");
			else
				this.msg.setText("<html>" + ERROR + "<br /> error found in pdfMaker..." + "</html>");
		}
		else
			this.msg.setText("<html>" + ERROR + "<br /> error found in imageMaker..." + "</html>");
		
		this.menu.setVisible(true);
		//cleanup
		File im = new File(this.imageloc);
		im.delete();
	}
	
	private boolean imageMaker() {
		try {
			Thread.sleep(3000);
			Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage capture = new Robot().createScreenCapture(screenRect);
			ImageIO.write(capture, "jpg", new File(this.imageloc));
			Thread.sleep(1000);
			return true;
		} 
		catch (AWTException e1) {
			e1.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean pdfMaker() {
		try {
			PDDocument document = new PDDocument();
			InputStream in = new FileInputStream(this.imageloc);
			BufferedImage bimg = ImageIO.read(in);
			float width = bimg.getWidth();
			float height = bimg.getHeight();
			PDPage page = new PDPage(new PDRectangle(width, height));
			document.addPage(page);
			
			String t = this.tfield.getText().trim();
			String s = (t.endsWith(".pdf"))? t : t + ".pdf"; 

			PDXObjectImage img = new PDJpeg(document, new FileInputStream(this.imageloc));
			PDPageContentStream contentStream = new PDPageContentStream(document,page);
			contentStream.drawImage(img, 0, 0);
			contentStream.close();
			in.close();
			
			document.save(this.loc + File.separator + s);
			document.close();
			return true;
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (COSVisitorException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args) throws AWTException, IOException, COSVisitorException {
		String home = System.getProperty("user.home");
		String s = home + File.separator + "Desktop" + File.separator + "invoice_mailer";
		File f = new File(s);
		if (!f.exists())
			f.mkdirs();
		
		new PDFScreenCatcher(s, "new1");

	}
}