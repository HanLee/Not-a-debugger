import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import shared.HookStates;
import shared.MethodInfo;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;


public class NADClient implements ActionListener{

	private JFrame frame;
	private JTextField textField;
	private JLabel labelHookedApplication;
	private JButton btnConnect;
	private JLabel lblNewLabel;
	private WebSocketClient wsc;
	private JTextArea textAreaLog;
	private JScrollPane scrollPane;
	private JTabbedPane tabbedPane;
	private JPanel panelApps;
	private JPanel panelFuncs;
	//private JPanel panelSystem;
	private JPanel panelOptions;
	private JList<String> listApps;
	private JTree treeFuncs;
	//private JTree treeSystemFuncs;
	DefaultMutableTreeNode root;
	DefaultMutableTreeNode systemTreeRoot;
	private ArrayList<String> arrayListHookableApplications;
	private DefaultListModel<String> defaultListModelApplications;
	private JScrollPane scrollPaneFuncs;
	private JScrollPane scrollPaneApps;
	private JScrollPane scrollPaneOptions;
	//private JScrollPane scrollPaneSystems;
	/*
	private ArrayList<Boolean> classNameListIndex;
	private ArrayList<MethodStatusObject> listTracker;
	*/
	private JTextField appSearchTextField;
	private JTextField functionSearchTextField;
	private JLabel traceModeLabel;
	private JToggleButton traceModeToggleButton;
	private JLabel canaryModeLabel;
	private JToggleButton canaryModeToggleButton;
	private JTextField canaryTokenTextField;
	
	private JLabel disableANRStatusLabel;
	private JToggleButton disableANRStatusButton;
	//private JTabbedPane systemFunctionsTabPane;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NADClient window = new NADClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NADClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 646, 658);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		JPanel connectPanel = new JPanel(new BorderLayout());
		textField = new JTextField("ws://192.168.108.181:8888");
		//textField.setBounds(10, 11, 161, 20);
		textField.setColumns(10);
		connectPanel.add(textField, BorderLayout.CENTER);
		
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(181, 10, 89, 23);
		connectPanel.add(btnConnect, BorderLayout.EAST);
		
		topPanel.add(connectPanel, BorderLayout.NORTH);
		//frame.getContentPane().add(btnConnect);
		btnConnect.addActionListener(this);
		
		JPanel currentHookedPanel = new JPanel(new BorderLayout());
		lblNewLabel = new JLabel("Current hooked: ");
		//lblNewLabel.setBounds(10, 52, 98, 14);
		currentHookedPanel.add(lblNewLabel, BorderLayout.WEST);
		
		labelHookedApplication = new JLabel("");
		//labelHookedApplication.setBounds(108, 52, 416, 14);
		currentHookedPanel.add(labelHookedApplication, BorderLayout.CENTER);
		
		topPanel.add(currentHookedPanel, BorderLayout.SOUTH);
		
		frame.getContentPane().add(topPanel, BorderLayout.NORTH);
		
		
		JPanel logPanel = new JPanel(new BorderLayout());
		
		JLabel lblLog = new JLabel("Log");
		//lblLog.setBounds(10, 77, 46, 14);
		logPanel.add(lblLog, BorderLayout.NORTH);
		
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		
		textAreaLog = new JTextArea();
		//textAreaLog.setBounds(10, 101, 263, 400);
		logPanel.add(textAreaLog, BorderLayout.CENTER);
		
		scrollPane = new JScrollPane(textAreaLog);
		//scrollPane.setBounds(10, 101, 263, 400);
		logPanel.add(scrollPane);
		
		splitPane.setLeftComponent(logPanel);
		
		
		JPanel selectionPanel = new JPanel(new BorderLayout());
				
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		//tabbedPane.setBounds(283, 77, 241, 424);
		//frame.getContentPane().add(tabbedPane);
		selectionPanel.add(tabbedPane, BorderLayout.CENTER);

		arrayListHookableApplications = new ArrayList<String>();
		//arrayListHookableFunctions = new ArrayList<String>();

		defaultListModelApplications = new DefaultListModel<String>();
		listApps = new JList<String>(defaultListModelApplications);
		panelApps = new JPanel();
		panelApps.setLayout(new BorderLayout());
		scrollPaneApps = new JScrollPane(listApps);
		panelApps.add(scrollPaneApps, BorderLayout.CENTER);
		
		//defaultListModelFunctions = new DefaultListModel<String>();
		//listFuncs = new JList<String>(defaultListModelFunctions);
		//listFuncs.setCellRenderer(new DisabledItemCellRenderer());
		//listFuncs.setSelectionModel(new DisabledItemSelectorModel());
		
		//tree
		MethodStatusObject msoRoot = new MethodStatusObject();
		//make it not detect double clicks
		msoRoot.setIsClassName(true);
		msoRoot.setClassName("Root");
		root = new DefaultMutableTreeNode(msoRoot);
		
		treeFuncs = new JTree(root);
		treeFuncs.setCellRenderer(new FuncTreeCellRenderer());
		
		panelFuncs = new JPanel();
		panelFuncs.setLayout(new BorderLayout());
		scrollPaneFuncs = new JScrollPane(/*listFuncs*/treeFuncs);
		panelFuncs.add(scrollPaneFuncs, BorderLayout.CENTER);
		
		tabbedPane.add("Apps", panelApps);
		
		appSearchTextField = new JTextField();
		panelApps.add(appSearchTextField, BorderLayout.NORTH);
		appSearchTextField.setColumns(10);
		tabbedPane.add("Functions", panelFuncs);
		
		//functionSearchTextField = new JTextField();
		//panelFuncs.add(functionSearchTextField, BorderLayout.NORTH);
		//functionSearchTextField.setColumns(10);
		
		
		//tabbedPane.add("System functions", panelApps);
		/*
		systemTreeRoot = new DefaultMutableTreeNode("Available System Functions");
		treeSystemFuncs = new JTree(systemTreeRoot);
		
		panelSystem = new JPanel();
		panelSystem.setLayout(new BorderLayout());
		scrollPaneSystems = new JScrollPane(treeSystemFuncs);
		panelSystem.add(scrollPaneSystems, BorderLayout.CENTER);
		tabbedPane.add("System", panelSystem);
		*/
		
		panelOptions = new JPanel();
		panelOptions.setLayout(new BorderLayout());
		JPanel optionsScrollInnerPanel = new JPanel(new GridBagLayout());
		/*
		JLabel traceModeLabel = new JLabel("Tracer");
		optionsScrollInnerPanel.add(traceModeLabel);
		JToggleButton traceMode = new JToggleButton("Tracer");
		optionsScrollInnerPanel.add(traceMode);
		*/
		scrollPaneOptions = new JScrollPane(optionsScrollInnerPanel);
		
		/*
		 * Trace Mode Option
		 */
		traceModeLabel = new JLabel("Trace mode");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		optionsScrollInnerPanel.add(traceModeLabel, gbc_lblNewLabel_1);
		
		traceModeToggleButton = new JToggleButton("on / off");
		GridBagConstraints gbc_tglbtnNewToggleButton = new GridBagConstraints();
		gbc_tglbtnNewToggleButton.gridx = 1;
		gbc_tglbtnNewToggleButton.gridy = 0;
		
		ActionListener traceModeButtonActionListener = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  JToggleButton abstractButton = (JToggleButton) actionEvent.getSource();
		        boolean selected = abstractButton.getModel().isSelected();
		        System.out.println("Action - selected=" + selected + "\n");
		        wsc.send("toggleTraceMode " + selected);
		        if(selected)
		        {
		        	ModeAlert.infoBox("You have just enabled Trace mode, remember to kill the current app process and run it again. Canary mode will be disabled.", "Trace mode");
		        	wsc.send("toggleCanaryMode " + false);
		        }
		        wsc.send("getTraceModeStatus");
				wsc.send("getCanaryModeStatus");
		      }
		    };
		    // Attach Listeners
		    traceModeToggleButton.addActionListener(traceModeButtonActionListener);
		
		optionsScrollInnerPanel.add(traceModeToggleButton, gbc_tglbtnNewToggleButton);
		
		canaryModeLabel = new JLabel("Canary Mode");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		optionsScrollInnerPanel.add(canaryModeLabel, gbc_lblNewLabel_2);
		
		canaryTokenTextField = new JTextField("d3adb33f");
		canaryTokenTextField.setColumns(8);
		GridBagConstraints gbc_canaryTokenTextField = new GridBagConstraints();
		gbc_canaryTokenTextField.gridx = 1;
		gbc_canaryTokenTextField.gridy = 1;
		optionsScrollInnerPanel.add(canaryTokenTextField, gbc_canaryTokenTextField);
		
		canaryModeToggleButton = new JToggleButton("on / off");
		GridBagConstraints gbc_canaryModeToggleButton = new GridBagConstraints();
		gbc_canaryModeToggleButton.gridx = 2;
		gbc_canaryModeToggleButton.gridy = 1;
		
		ActionListener toggleCanaryModeButtonActionListener = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  JToggleButton abstractButton = (JToggleButton) actionEvent.getSource();
		        boolean selected = abstractButton.getModel().isSelected();
		        System.out.println("Action - selected=" + selected + "\n");
		        wsc.send("toggleCanaryMode " + selected);
		        wsc.send("setCanaryToken " + canaryTokenTextField.getText());
		        if(selected)
		        {
		        	ModeAlert.infoBox("You have just enabled Canary mode, remember to kill the current app process and run it again. Trace mode will be disabled.", "Canary mode");
		        	wsc.send("toggleTraceMode " + false);
		        }
		        wsc.send("getTraceModeStatus");
				wsc.send("getCanaryModeStatus");
		      }
		    };
		    // Attach Listeners
		    canaryModeToggleButton.addActionListener(toggleCanaryModeButtonActionListener);
		    optionsScrollInnerPanel.add(canaryModeToggleButton, gbc_canaryModeToggleButton);

		/*
		 * disableANR Option - I may implement this option to enable/disable it eventually.
		 */
		/*
		disableANRStatusLabel = new JLabel("disable ANR");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		optionsScrollInnerPanel.add(disableANRStatusLabel, gbc_lblNewLabel_2);
		
		disableANRStatusButton = new JToggleButton("on / off");
		GridBagConstraints gbc_tglbtnNewToggleButton1 = new GridBagConstraints();
		gbc_tglbtnNewToggleButton1.gridx = 1;
		gbc_tglbtnNewToggleButton1.gridy = 1;
		
		ActionListener disableANRStatusButtonActionListener = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  JToggleButton abstractButton = (JToggleButton) actionEvent.getSource();
		        boolean selected = abstractButton.getModel().isSelected();
		        System.out.println("Action - selected=" + selected + "\n");
		        wsc.send("toggleANRStatus " + selected);
		      }
		    };
		    // Attach Listeners
		    disableANRStatusButton.addActionListener(disableANRStatusButtonActionListener);
		
		optionsScrollInnerPanel.add(disableANRStatusButton, gbc_tglbtnNewToggleButton1);
		*/
		
		
		
		panelOptions.add(scrollPaneOptions, BorderLayout.CENTER);
		tabbedPane.add("Options", panelOptions);
		
		
		splitPane.setRightComponent(selectionPanel);
		
		splitPane.setResizeWeight(0.5);
		
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		MouseListener appMouseListener = new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) {

		           String selectedItem = (String) listApps.getSelectedValue();
		           int response = JOptionPane.showConfirmDialog(null, "Do you want to hook "+selectedItem+"?", "Confirm",
		        	        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		        	    if (response == JOptionPane.YES_OPTION) {
		        	      wsc.send("hookPackage " + selectedItem);
		        	    }
		         }
		    }
		};
		listApps.addMouseListener(appMouseListener);
		
		MouseListener funcMouseListener = new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
		         int selRow = treeFuncs.getRowForLocation(e.getX(), e.getY());
		         TreePath selPath = treeFuncs.getPathForLocation(e.getX(), e.getY());
		         if(selRow != -1) {
		             if(e.getClickCount() == 2) {
		                 //myDoubleClick(selRow, selPath);
		            	 if(selPath != null)
		            	 {
		            		 DefaultMutableTreeNode methodNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
		            		 
		            		 MethodStatusObject mso = (MethodStatusObject) methodNode.getUserObject();
		            		 if(!mso.getIsClassName())
		            		 {
		            			 System.out.println("parent: " + methodNode.getParent());
		            			 System.out.println("got it : " + mso.getMethodName());
		            			 
		            			 
		            			JPanel panel = new JPanel();
		     	                panel.add(new JLabel("Please make a selection:"));
		     	                DefaultComboBoxModel model = new DefaultComboBoxModel();
		     	                model.addElement("Hook Input Parameters Only");
		     	                model.addElement("Hook Return Value Only");
		     	                model.addElement("Hook Input and Return Values");
		     	                //if(mso.getHooked())
		     	                	model.addElement("Remove hook");
		     	                JComboBox comboBox = new JComboBox(model);
		     	                panel.add(comboBox);
		     	                comboBox.getSelectedIndex();
		     		           int response = JOptionPane.showConfirmDialog(null, panel, "Do you want to hook "+methodNode.getParent()+":"+mso.getMethodName()+"?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		     		           HookStates hookState = null;
		     		           switch(comboBox.getSelectedIndex()){
		     		           		case 0: hookState = HookStates.HOOK_INPUT_ONLY;
		     		           		break;
		     		           		
		     		           		case 1: hookState = HookStates.HOOK_RETURN_ONLY;
		     		           		break;
		     		           		
		     		           		case 2: hookState = HookStates.HOOK_ALL;
		     		           		break;
		     		           		
		     		           		case 3: hookState = HookStates.HOOK_NONE;
		     		           		break;
		     		           		
		     		           		default: hookState = HookStates.HOOK_ALL;
		     		           		break;
		     		           	
		     		           }
		     		        	    if (response == JOptionPane.YES_OPTION) {
		     		        	    	//han: need - instead of ; cause arrays need ; in the representation
		     		        	      wsc.send("hookMethod " + methodNode.getParent()+":"+mso.getMethodName() + "-" + hookState);
		     		        	    }
		            			 
		            		  
		            		 }
		            	 }
		             }
		         }
		     }
		    
		};
		treeFuncs.addMouseListener(funcMouseListener);
		
		appSearchTextField.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent arg0) {}

			@Override
			public void insertUpdate(DocumentEvent arg0) {filter();}

			@Override
			public void removeUpdate(DocumentEvent arg0) {filter();}
			
			private void filter(){
				filterModel((DefaultListModel<String>)listApps.getModel(), appSearchTextField.getText(), arrayListHookableApplications);
			}
			
		});

		/*
		functionSearchTextField.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent arg0) {}

			@Override
			public void insertUpdate(DocumentEvent arg0) {filter();}

			@Override
			public void removeUpdate(DocumentEvent arg0) {filter();}
			
			private void filter(){
				System.out.println("in filter");
				//filterModel1((DefaultListModel<String>)listFuncs.getModel(), functionSearchTextField.getText(), listTracker);
			}
			
		});
		*/
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == btnConnect){
			System.out.println("clicky!");
			URI uri = null;
			try {
				uri = new URI(textField.getText());
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			wsc = new WebSocketClient( uri, new Draft_17() ){

				@Override
				public void onOpen(ServerHandshake handshakedata) {
					// TODO Auto-generated method stub
					btnConnect.setText("Disconnect");
					wsc.send("showPackages");
					//get nad system functions
					//wsc.send("showSystemFunctions");
					wsc.send("getTraceModeStatus");
					wsc.send("getCanaryModeStatus");
					wsc.send("getCanaryToken");
					//wsc.send("getANRStatus");
				}

				@Override
				public void onMessage(String message) {
					// TODO Auto-generated method stub
					System.out.println("Message received: " + message);
					JSONObject x = new JSONObject(message);
					String type = x.getString("type");
					if(type.equals("log"))
					{
						textAreaLog.setText(x.getString("data") + "\n" + textAreaLog.getText());
					}
					if(type.equals("currentHookedApplication"))
					{
						labelHookedApplication.setText(x.getString("data"));
					}
					if(type.equals("hookableFunctions"))
					{
						
						root.removeAllChildren();
						DefaultTreeModel dtm =  (DefaultTreeModel) treeFuncs.getModel();
						dtm.reload();
						
						final JSONArray jsonArray = x.getJSONArray("data");
						
						
						Runnable runner = new Runnable () 
				        {
				            public void run () 
				            {
				            	
				            	JSONArray sortedJsonArray = sortJSONArray(jsonArray);
				            	
				            	for(int i=0; i<sortedJsonArray.length(); i++)
								{
				            		
				            		JSONObject jo = sortedJsonArray.getJSONObject(i);
									String className = jo.getString("class");
									
									MethodStatusObject mso1 = new MethodStatusObject();
									mso1.setIsClassName(true);
									mso1.setClassName(className);
									DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(mso1);
									root.add(classNode);
									
									
									JSONArray ja = jo.getJSONArray("methods");
									for(int x =0; x<ja.length(); x++){
										String methodName = ja.get(x).toString();
										
										MethodStatusObject mso2 = new MethodStatusObject();
										mso2.setMethodName(methodName);
										DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(mso2);
										classNode.add(methodNode);
									}
									
								}
				            }
				        };
				        EventQueue.invokeLater (runner);
				        
				        wsc.send("showHookedMethods");
					}
					if(type.equals("hookableApplications"))
					{
						arrayListHookableApplications.clear();
						defaultListModelApplications.removeAllElements();
						final JSONArray jsonArray = x.getJSONArray("data");
						
						Runnable runner = new Runnable () 
				        {
				            public void run () 
				            {
				            	for(int i=0; i<jsonArray.length(); i++)
								{
									String app = jsonArray.get(i).toString();
									arrayListHookableApplications.add(app);
									defaultListModelApplications.addElement(app);
								}
				            }
				        };
				        EventQueue.invokeLater (runner);				
					}
					if(type.equals("methodInfo"))
					{
						System.out.println("got method info");
						String methodInfoString = x.getString("data");
						System.out.println("methodInfoString1: " + methodInfoString);
						
						JsonObject s = new Gson().fromJson(message, JsonObject.class);
						System.out.println("methodInfoString2: " + s.get("data").getAsString());

						
						byte[] ba = Base64.getDecoder().decode(methodInfoString);
						ByteArrayInputStream in = new ByteArrayInputStream(ba);
					    ObjectInputStream is = null;
						try {
							is = new ObjectInputStream(in);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    MethodInfo mi = null;
					    
						try {
							mi = (MethodInfo) is.readObject();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					    System.out.println("Got method: " + mi.getHumanReadableDescription());
					    
					    MethodInfoEditor mie = new MethodInfoEditor(mi);

						mie.setVisible(true);
						MethodInfo mi2 = mie.getMethodInfo();

					    mie.dispose();
					    
					    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	                    ObjectOutput out = null;
	                    byte[] editedMethodInfoBytes = null;
	                    try {
	                        out = new ObjectOutputStream(bos);
	                        out.writeObject(mi2);
	                        editedMethodInfoBytes = bos.toByteArray();
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    } finally {
	                        try {
	                            if (out != null) {
	                                out.close();
	                            }
	                        } catch (IOException ex) {
	                            // ignore close exception
	                        }
	                        try {
	                            bos.close();
	                        } catch (IOException ex) {
	                            // ignore close exception
	                        }
	                    }
	                    
	                    String encodedEditedMethodInfo = Base64.getEncoder().encodeToString(editedMethodInfoBytes);
					    System.out.println("edited method string: " + encodedEditedMethodInfo);
					    wsc.send("methodInfo " + encodedEditedMethodInfo);
					}
					if(type.equals("hookedMethods"))
					{
						final JSONArray jsonArray = x.getJSONArray("data");
						System.out.println("hookedmethods!");
						Runnable runner = new Runnable () 
				        {
				            public void run () 
				            {
				            	clearTree();
								for(int i=0; i<jsonArray.length(); i++)
								{
									JSONObject jo = jsonArray.getJSONObject(i);
									String className = jo.getString("class");
									JSONArray ja = jo.getJSONArray("methods");
									ArrayList<String> methods = new ArrayList<String>();
									for(int x=0; x<ja.length();x++)
										methods.add(ja.getString(x));
									findAndUpdateMethodStatus(root, className, methods);
								}
								treeFuncs.repaint();
				            }
				        };
				        EventQueue.invokeLater (runner);
					}
					if(type.equals("availableSystemMethods"))
					{
						System.out.println("availablesystemMethods!");

						HashMap<String, HashMap<String, String>> availableSystemMethods = new HashMap<>();
						JSONArray availableSystemFunctionsJSONArray = null;
						try{
							String data = x.getString("data");
							availableSystemFunctionsJSONArray = new JSONArray(data);
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
						}
						for (int i=0; i<availableSystemFunctionsJSONArray.length(); i++)
						{
							JSONObject systemFunctionDescriptionJSONObject = null;
							try{
								systemFunctionDescriptionJSONObject = availableSystemFunctionsJSONArray.getJSONObject(i);
							}
							catch(Exception ex)
							{
								System.out.println(ex.toString());
							}
							Iterator<?> keys = systemFunctionDescriptionJSONObject.keys();
							
							while( keys.hasNext() ) {
							    String key = (String)keys.next();
							    HashMap<String, String> functionDescriptionHM = new HashMap<>();
							    if ( systemFunctionDescriptionJSONObject.get(key) instanceof JSONArray ) {
									System.out.println("> " + key);
									JSONArray systemFunctionMethods = null;
									systemFunctionMethods = systemFunctionDescriptionJSONObject.getJSONArray(key);
									
									for(int i1=0; i1<systemFunctionMethods.length(); i1++)
									{
										JSONObject methodObject = (JSONObject) systemFunctionMethods.get(i1);
										Iterator<?> keys1 = methodObject.keys();
										while( keys1.hasNext() ) {
										    String key1 = (String)keys1.next();
										    System.out.println("index: " + key1 + " : " + methodObject.get(key1));
										    functionDescriptionHM.put(key1, (String) methodObject.get(key1));
										}
									}
									availableSystemMethods.put(key, functionDescriptionHM);
							    }
							}
						}
						//systemTreeRoot.add(newChild);
						for(Map.Entry<String, HashMap<String, String>> entry : availableSystemMethods.entrySet())
						{
							DefaultMutableTreeNode function = new DefaultMutableTreeNode(entry.getKey());
							
							for(Map.Entry<String, String> methodDescription :  entry.getValue().entrySet())
							{
								function.add(new DefaultMutableTreeNode(methodDescription.getKey() + " : " + methodDescription.getValue()));
							}
							systemTreeRoot.add(function);
						}
					}
					//listFuncs.repaint();
					if(type.equals("traceModeStatus"))
					{
						System.out.println("traceModeStatus!");
						try{
							String data = x.getString("data");
							if(data.equals("true"))
							{
								traceModeToggleButton.setSelected(true);
								ModeAlert.infoBox("Trace mode is turned on, you wont be able to perform on-the-fly hooking.", "Trace mode");
							}
							else
								traceModeToggleButton.setSelected(false);
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
						}
					}
					if(type.equals("canaryModeStatus"))
					{
						System.out.println("canaryModeStatus!");
						try{
							String data = x.getString("data");
							if(data.equals("true"))
							{
								canaryModeToggleButton.setSelected(true);
								ModeAlert.infoBox("Canary mode is turned on, you wont be able to perform on-the-fly hooking.", "Canary mode");
							}
							else
								canaryModeToggleButton.setSelected(false);
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
						}
					}
					if(type.equals("canaryToken"))
					{
						System.out.println("canaryToken!");
						try{
							String data = x.getString("data");
							canaryTokenTextField.setText(data);
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
						}
					}
					/*
					if(type.equals("disableANRStatus"))
					{
						System.out.println("disableANRStatus!");
						try{
							String data = x.getString("data");
							if(data.equals("true"))
								traceModeToggleButton.setSelected(true);
							else
								traceModeToggleButton.setSelected(false);
						}
						catch(Exception ex)
						{
							System.out.println(ex.toString());
						}
					}
					*/
				}
				
				
				@Override
				public void onClose(int code, String reason, boolean remote) {
					// TODO Auto-generated method stub
					btnConnect.setText("Connect");
				}

				@Override
				public void onError(Exception ex) {
					// TODO Auto-generated method stub
					
				}};
				
				wsc.connect();
				
		}
		
	}
	
	public void filterModel(DefaultListModel<String> model, String filter, ArrayList<String> arrayList) {
	    for (String s : arrayList) {
	        if (!s.startsWith(filter) && !s.contains(filter)) {
	            if (model.contains(s)) {
	                model.removeElement(s);
	            }
	        } else {
	            if (!model.contains(s)) {
	                model.addElement(s);
	            }
	        }
	    }
	}
	
	public void filterModel1(DefaultListModel<String> model, String filter, ArrayList<MethodStatusObject> arrayList) {
		for (MethodStatusObject o : arrayList)
		{
			String text = "";
			if(o.getIsClassName())
			{
				text = o.getClassName();
			}
			else if(!o.getIsClassName())
			{
				text = o.getMethodName();
			}
			
			if(!text.startsWith(filter) && !text.contains(filter))
			{
				if (model.contains(text)){
					model.removeElement(text);
				}
			}
			else
			{
				if(!model.contains(text)){
					model.addElement(text);
				}
			}
		}

	}
	
	public JSONArray sortJSONArray(JSONArray jsonArray)
	{
		JSONArray sortedJsonArray = new JSONArray();

	    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
	    for (int i = 0; i < jsonArray.length(); i++) {
	        jsonValues.add(jsonArray.getJSONObject(i));
	    }
	    Collections.sort( jsonValues, new Comparator<JSONObject>() {
	        private static final String KEY_NAME = "class";

	        @Override
	        public int compare(JSONObject a, JSONObject b) {
	            String valA = new String();
	            String valB = new String();

	            try {
	                valA = (String) a.get(KEY_NAME);
	                valB = (String) b.get(KEY_NAME);
	            } 
	            catch (JSONException e) {
	            }

	            return valA.compareTo(valB);

	        }
	    });
	    
	    for (int i = 0; i < jsonArray.length(); i++) {
	        sortedJsonArray.put(jsonValues.get(i));
	    }
	    
	    return sortedJsonArray;
	}
	
	public void findAndUpdateMethodStatus(DefaultMutableTreeNode root, String className, ArrayList<String> methodNames) {
	    @SuppressWarnings("unchecked")
	    Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration();
	    
	    while(e.hasMoreElements())
	    {
	    	Boolean colorClass = false;
	    	DefaultMutableTreeNode dmtn = e.nextElement();
	    	MethodStatusObject msoClass = (MethodStatusObject) dmtn.getUserObject();
	    	
	    	if(msoClass.getIsClassName() && msoClass.getClassName().equals(className))
	    	{
	    		Enumeration<DefaultMutableTreeNode> m = dmtn.children();
	    		while(m.hasMoreElements())
	    		{
	    			DefaultMutableTreeNode methodNode = m.nextElement();
	    			MethodStatusObject msoMethod = (MethodStatusObject) methodNode.getUserObject();

	    			if(methodNames.contains(msoMethod.getMethodName()))
	    			{
	    				msoMethod.setHooked(true);
	    				((DefaultTreeModel)treeFuncs.getModel()).nodeChanged(methodNode);
	    				colorClass = true;
	    			}
	    			if(colorClass)
	    			{
	    				msoClass.setHooked(true);
	    				treeFuncs.repaint();
	    			}
	    		}
	    	}
	    }
	}
	
	public void clearTree()
	{
		Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration();
	    
	    while(e.hasMoreElements())
	    {
	    	DefaultMutableTreeNode dmtn = e.nextElement();
	    	MethodStatusObject msoClass = (MethodStatusObject) dmtn.getUserObject();
	    	
	    	if(msoClass.getIsClassName())
	    	{
	    		Enumeration<DefaultMutableTreeNode> m = dmtn.children();
	    		while(m.hasMoreElements())
	    		{
	    			DefaultMutableTreeNode methodNode = m.nextElement();
	    			MethodStatusObject msoMethod = (MethodStatusObject) methodNode.getUserObject();

	    			if(msoMethod.getHooked())
	    			{
	    				msoMethod.setHooked(false);
	    				((DefaultTreeModel)treeFuncs.getModel()).nodeChanged(methodNode);
	    			}
	    		}
	    	}
	    }
	}
	
}
