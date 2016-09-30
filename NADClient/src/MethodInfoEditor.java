import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import shared.MethodInfo;
import shared.ParameterInfo;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MethodInfoEditor extends JDialog implements ActionListener {

	private final JPanel contentPanel = new JPanel();
	//private ArrayList<JTextField> textFields = new ArrayList<JTextField>();
	private ArrayList<JTextArea> textAreas = new ArrayList<JTextArea>();
	private JButton okButton;
	private MethodInfo methodInfo;
	/**
	 * Launch the application.
	 */
	/*
	public static void main(String[] args) {
		try {
			MethodInfoEditor dialog = new MethodInfoEditor();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	public MethodInfo getMethodInfo() {
		return methodInfo;
	}
	/**
	 * Create the dialog.
	 */
	public MethodInfoEditor(MethodInfo mi) {
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout());
		JLabel lblMethodDescription = null;
		this.methodInfo = mi;
		{
			lblMethodDescription = new JLabel("New label");
			lblMethodDescription.setBounds(10, 11, 414, 14);
			contentPanel.add(lblMethodDescription);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(10, 35, 414, 183);
			JPanel panel = new JPanel(new GridBagLayout());
			
			/*
			for(int i=0; i< mi.getMethodParameterTypes().size(); i++)
		    {
				JTextField textField = new JTextField(mi.getMethodParameterValues().get(i).toString(), 20);
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 1;
	            constraints.gridy = i;
	            constraints.insets = new Insets(10,10,10,10);
				panel.add(textField, constraints);
				
				textFields.add(textField);
				
				Class<?> tmpClass = (Class<?>) mi.getMethodParameterTypes().get(i);
				String simpleName = tmpClass.getSimpleName();
				
				JLabel label = null;
				if (mi.getHookableParamsIndex().get(i).toString().equals("-1"))
					label = new JLabel("[Return Value]");
				else
					label = new JLabel("[" + mi.getHookableParamsIndex().get(i).toString() +"] " + simpleName);
				constraints.gridx = 0;
				constraints.gridy = i;
				panel.add(label, constraints);
			}
			*/
			lblMethodDescription.setText(mi.getClassName()+":"+mi.getHumanReadableDescription());
			System.out.println("debug size: " + mi.getMethodParameters().size());
			for(int i=0; i < mi.getMethodParameters().size(); i++)
			{
				ParameterInfo pi = mi.getMethodParameters().get(i);
				//System.out.println("debug: " + pi.getParameterValue().toString());
				//JTextField textField = new JTextField(pi.getParameterValue().toString(), 20);
				String string = pi.getParameterValue().toString();
				int rows = 1;
				
				if(string.contains("\n"))
				{
					for(int l=0; l<string.length(); l++)
					{
						if(string.charAt(l) == '\n')
							rows++;
					}
				}
				else
				{
					rows = 1;
				}
				
				JTextArea textArea = new JTextArea(pi.getParameterValue().toString(), rows, 20);
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 1;
	            constraints.gridy = i;
	            constraints.insets = new Insets(10,10,10,10);
				//panel.add(textField, constraints);
	            panel.add(textArea, constraints);
				
				//textFields.add(textField);
	            textAreas.add(textArea);
				
				//String simpleName = pi.getParameterClass().getSimpleName();
				String simpleName = pi.getParameterClass();
				JLabel label = null;
				if(!pi.getIsReturnValue())
					label = new JLabel("[" + pi.getParameterIndex().toString() +"] " + simpleName);
				else if(pi.getIsReturnValue())
					label = new JLabel("[Return Value]");
				constraints.gridx = 0;
				constraints.gridy = i;
				panel.add(label, constraints);
			}
			
			scrollPane.setViewportView(panel);
			contentPanel.add(scrollPane, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object object = e.getSource();
	
		        if( object.equals( okButton ) )
		        {
		        	/*
		            ArrayList<Object> valuesArray = new ArrayList<Object>();
		            for(int i=0; i<this.methodInfo.getMethodParameterValues().size(); i++)
		            {
		            	valuesArray.add(textFields.get(i).getText());
		            	System.out.println("asd: " + textFields.get(i).getText());
		            }
		            this.methodInfo.setMethodParameterValues(valuesArray);
		            setVisible(false);
		            */
		        	ArrayList<ParameterInfo> parameterArray = new ArrayList<ParameterInfo>();
		        	for(int i=0; i<this.methodInfo.getMethodParameters().size(); i++)
		        	{
		        		this.methodInfo.getMethodParameters().get(i).setParameterValue(/*textFields.get(i).getText()*/textAreas.get(i).getText());
		        	}
		        	setVisible(false);
		        }
	}

}
