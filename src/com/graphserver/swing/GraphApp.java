package com.graphserver.swing;

import static com.graphserver.data.FileUtil.getHeaders;
import static com.graphserver.data.FileUtil.readCSV;
import static com.graphserver.module.monetdb.dao.MonetDbDao.ID;
import static com.graphserver.module.monetdb.dao.MonetDbDao.INT;
import static com.graphserver.module.monetdb.dao.MonetDbDao.NODESINFO;
import static com.graphserver.module.monetdb.dao.MonetDbDao.RECORDSINFO;
import static com.graphserver.module.monetdb.dao.MonetDbDao.VARCHAR;
import static com.graphserver.module.monetdb.dao.MonetDbDao.createTable;
import static com.graphserver.module.monetdb.dao.MonetDbDao.dbClose;
import static com.graphserver.module.monetdb.dao.MonetDbDao.dbInit;
import static com.graphserver.module.monetdb.dao.MonetDbDao.dropTable;
import static com.graphserver.module.monetdb.dao.MonetDbDao.insertValuesToTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.graphserver.data.FileUtil;
import com.graphserver.graph.Functions;
import com.graphserver.graph.GraphEngine;
import com.graphserver.graph.Records;
import com.graphserver.graph.Topology;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class GraphApp extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private static final String version = "v.1.0.0";
	
    public static final String SUM 					= "SUM";
    public static final String MIN 					= "MIN";
    public static final String MAX 					= "MAX";
    public static final String AVG 					= "AVG";
	
    private static final String topologyLabel 			= "Choose Topology File";
	private static final String recordsLabel 			= "Choose Records File";
	private static final String nodesLabel 				= "Choose Nodes File";
	private static final String edgesLabel 				= "Choose Edges File";
	private static final String COMMA 					= "COMMA";
	private static final String SEMICOLON 				= "SEMICOLON";
	private static final String VERTICAL_BAR 			= "VERTICAL_BAR";
	private static final String topologyDeli 			= "TopologyDeli";
	private static final String recordsDeli 			= "RecordsDeli";
	private static final String nodesDeli 				= "NodesDeli";
	private static final String edgesDeli 				= "EdgesDeli";
	private static final String topologyHeader 			= "TopologyHeader";
	private static final String recordsHeader 			= "RecordsHeader";
	private static final String recordsIds 				= "RecordsIds";
	private static final String topohelp 				= "Topology Help";
	private static final String recohelp 				= "Records Help";
	private static final String	nodehelp 				= "Nodes Help";
	private static final String edgehelp 				= "Edges Help";
	private static final String finishlabel 			= "Finish";	
	private static final String[] delis 				= new String[] { COMMA, SEMICOLON, VERTICAL_BAR };
	private static final Map<String, String> delisMap 	= new HashMap<>();
	
	private JFrame 			pleasewaitframe;
	private Topology 		topology;
	private Records 		records;
	private JLabel 			statusbar;
	private JPanel 			datapanel;
	private final String[] 	funsList 		= new String[]{SUM,MIN,MAX,AVG};
	private String 			funresedges;
	private String 			funresalledges;
	private String 			selectedFun   	= SUM;
	private String 			selectedRecId 	= "-1";
	private Set<String> 	uniqueedges;
	private String[][] 		allEdges;
	private List<String> 	uniqueNodes;
	private List<String> 	indexedEdges;
	private String[] 		allrecids;
	private JFrame 			insertframe;	
	private String 			topologyFile 		= null;
	private String 			recordsFile 		= null;
	private String 			nodesFile 			= null;
	private String 			edgesFile 			= null;
	private String 			topologyDel 		= COMMA;
	private String 			recordsDel 			= COMMA;
	private String 			nodesDel 			= COMMA;
	private String 			edgesDel 			= COMMA;
	private boolean 		topologyHEADER 		= false;
	private boolean 		recordsHEADER 		= false;
	private boolean 		recordsIDS 			= false;
	private String[] 		BVedges 			= null;
	private String[][] 		recEdges 			= null;	
	private String[] 		recordsinfoheaders 	= null;
	private String[] 		nodesinfoheaders   	= null;
	private Map<String, Double> allqueryvalues;
	private static boolean 	enableRecord		= false;
	private static boolean 	enableRecordIds 	= false;
	static{
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
	public GraphApp(){
		uniqueedges 	= new HashSet<>();
		indexedEdges 	= new ArrayList<>();
		funresedges 	= "-not selected yet-";
		funresalledges 	= "-not selected yet-";
		initUI();
	}
	public final void secondUI(){
		/*
		 * repaint JFrame
		 */
		getContentPane().invalidate();
		getContentPane().removeAll();		
		
		setLayout(new BorderLayout());
		JPanel north 		 = new JPanel(new BorderLayout());
		JPanel center 		 = new JPanel(new BorderLayout());
		JPanel south 		 = new JPanel(new FlowLayout());
		JPanel topNorth 	 = new JPanel(new BorderLayout());
		JPanel centerNorth   = new JPanel(new BorderLayout());
		JPanel verticalRight = new JPanel(new BorderLayout());
		
		createMenuBar();
			
		/*
		 * Show Topology Button
		 */
		JButton showTopo = new JButton("Show Topology");
		showTopo.addActionListener(new ActionListener5());
		topNorth.add(showTopo, BorderLayout.NORTH);
		
		/*
		 * Answer Label
		 */
		JLabel answerLabel = new JLabel("Answer:");
		answerLabel.setFont(new Font("Serif", Font.BOLD, 16));
		JScrollPane answerpane = new JScrollPane();
		answerpane.getViewport().add(answerLabel);
		if (enableRecord) {
			topNorth.add(answerpane, BorderLayout.CENTER);
		}
		
		/*
		 * Answer Panel
		 */
		StringBuilder answer = new StringBuilder();
		answer.append("<html><b>Function result for selected edges:</b>").append("<br>")
			  .append("<i>" + funresedges + "</i>").append("<br>").append("<b>Function result for all edges:</b>")
			  .append("<br>").append("<i>" + funresalledges + "</i>").append("<br></html>");
		JLabel answerPane = new JLabel(answer.toString());
		answerPane.setFont(new Font("Serif", Font.BOLD, 16));
		if (enableRecord) {
			topNorth.add(answerPane, BorderLayout.SOUTH);
		}
		
		/*
		 * Functions Label
		 */
		JLabel funlabel = new JLabel("Select function:");
		funlabel.setFont(new Font("Serif", Font.BOLD, 16));
		if (enableRecordIds) {
			centerNorth.add(funlabel, BorderLayout.NORTH);
		}
		
		/*
		 * Functions Panel
		 */
		JComboBox<String> funs = new JComboBox<>(funsList);
		funs.addItemListener(new ItemStateChanged1());
		if (enableRecordIds) {
			centerNorth.add(funs, BorderLayout.CENTER);
		}
		
		north.add(topNorth	  , BorderLayout.NORTH);
		north.add(centerNorth , BorderLayout.CENTER);
		
		/*
		 * RecorIds label
		 */
        JLabel recordIdslabel = new JLabel("Select record id:");
        recordIdslabel.setFont(new Font("Serif", Font.ITALIC, 16));
        recordIdslabel.setPreferredSize(new Dimension(0,40));
        if (enableRecordIds) {
			center.add(recordIdslabel, BorderLayout.NORTH);
		}
		/*
		 * JList recordIds
		 */
		if (enableRecordIds) {
			JScrollPane pane;
			JList<String> list = new JList<>(allrecids);
			list.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						String recid = list.getSelectedValue();
						selectedRecId = recid;
					}
				}
			});
			pane = new JScrollPane(list);
			pane.getViewport().add(list);
			pane.setPreferredSize(new Dimension(200, 500));
			center.add(pane, BorderLayout.CENTER);
		}
		
		JPanel forPanel;
		if (enableRecord) {
			//set parameters for record graph			 
			ShowGraph.recordId 		= selectedRecId;
			ShowGraph.recordsEdges 	= recEdges;
			ShowGraph.record		= true;
			ShowGraph.noRecordId	= false;
		} else {
			//reset record graph parameters			
			ShowGraph.recordId 		= null;
			ShowGraph.recordsEdges 	= null;
			ShowGraph.record		= false;
			ShowGraph.noRecordId	= true;
		}
		GraphEngine engine = new GraphEngine();
		engine.createGraph(uniqueNodes, allEdges);
		DirectedSparseGraph<String, Number> graph = engine.getGraph();
		String graphLabel = enableRecord ? "Record " + selectedRecId + " Graph" : "Topology Graph";

		forPanel = ShowGraph.initVV(graphLabel, graph);

		if (enableRecordIds) { 
	        JButton go	  = new JButton("Go");
	        JButton close = new JButton("Close");
	        go.addActionListener(new ActionListener4());	        
	        close.addActionListener(new ActionListener2());
	        south.add(go);
	        south.add(close);
		} else {
			JButton ok 	 = new JButton("Show Records");
			JButton exit = new JButton("Exit");
        	ok.addActionListener(new ActionListener1());	
        	exit.addActionListener(new ActionListener2());
        	south.add(ok);
        	south.add(exit);
		}

		verticalRight.setPreferredSize(new Dimension(200, 0));
		verticalRight.add(north  , BorderLayout.NORTH);
		verticalRight.add(center , BorderLayout.CENTER);
		verticalRight.add(south  , BorderLayout.SOUTH);
		
       	getContentPane().add(verticalRight	, BorderLayout.EAST);
        getContentPane().add(forPanel		, BorderLayout.CENTER);

        setTitle("GraphServer " + version + " - Welcome Page");
        setSize(1500, 850);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(()->{
        	getContentPane().validate();
        	getContentPane().repaint();
        });
	}
	public final void initUI(){
		System.out.println("Swing UI() started.");
		setLayout(null);		
		createMenuBar();                
        pack();
        setTitle("GraphServer " + version + " - Welcome Page");
        setSize(1500, 850);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null); 
	}
	private void loadData() throws IOException {
		System.out.println("Previous topology and records loaded.");
		topology = new Topology();
		topology.sameTopology();
		allEdges = topology.getAllNewEdges();
		uniqueNodes = topology.getUniqueNodes();
		records = new Records();
		records.sameRecords();
		indexedEdges = comboEdges( allEdges );
		nodesinfoheaders   = FileUtil.readHeadersFromMetadataFile( "metadata/nodesinfoheaders.txt" ).toArray(new String[0]);
		ShowGraph.nodesInfoHeaders = nodesinfoheaders;
		recordsinfoheaders = FileUtil.readHeadersFromMetadataFile( "metadata/recordsinfoheaders.txt" ).toArray(new String[0]);
		ShowGraph.recordsInfoHeaders = recordsinfoheaders;
		secondUI();
	}
	private List<String> comboEdges(String[][] edgs){
		List<String> ie = new ArrayList<>();
		Arrays.asList(edgs).forEach(e->{
			StringBuilder comb = new StringBuilder();
			comb.append(e[0]).append("to").append(e[1]);
			ie.add(comb.toString());
		});
		return ie;
	}
	private void createMenuBar() {
		JMenuBar menubar = new JMenuBar();

		ImageIcon iconExit = new ImageIcon("exit.png");
		ImageIcon iconNew  = new ImageIcon("new.png");
		ImageIcon iconLoad = new ImageIcon("load.png");

		JMenu session = new JMenu("Session");
		JMenu help    = new JMenu("Help");

		JMenuItem exitMenu = new JMenuItem("Exit", iconExit);
		JMenuItem newMenu  = new JMenuItem("New", iconNew);
		JMenuItem loadMenu = new JMenuItem("Load", iconLoad);

		exitMenu.setMnemonic(KeyEvent.VK_E);
		exitMenu.setToolTipText("Exit application");
		exitMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		newMenu.setMnemonic(KeyEvent.VK_N);
		newMenu.setToolTipText("New application");
		newMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				insertData();
			}
		});
		
		loadMenu.setMnemonic(KeyEvent.VK_L);
		loadMenu.setToolTipText("Load previous saved session");
		loadMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					loadData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		session.add(newMenu);
		session.add(loadMenu);
		session.addSeparator();
		session.add(exitMenu);

		menubar.add(session);
		menubar.add(help);

		setJMenuBar(menubar);
	}
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			JButton o = (JButton) e.getSource();
			String label = o.getText();
			if (label.equals(topologyLabel)) {
				statusbar.setText(" " + label + " button clicked");
				JFileChooser fdia = new JFileChooser();
				int ret = fdia.showDialog(datapanel, "Open file");

				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fdia.getSelectedFile();
					topologyFile = file.getAbsolutePath();
				}
			} else if (label.equals(recordsLabel)) {
				statusbar.setText(" " + label + " button clicked");
				JFileChooser fdia = new JFileChooser();
				int ret = fdia.showDialog(datapanel, "Open file");

				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fdia.getSelectedFile();
					recordsFile = file.getAbsolutePath();
				}
			} else if (label.equals(nodesLabel)) {
				statusbar.setText(" " + label + " button clicked");
				JFileChooser fdia = new JFileChooser();
				int ret = fdia.showDialog(datapanel, "Open file");

				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fdia.getSelectedFile();
					nodesFile = file.getAbsolutePath();
				}
			} else if (label.equals(edgesLabel)) {
				statusbar.setText(" " + label + " button clicked");
				JFileChooser fdia = new JFileChooser();
				int ret = fdia.showDialog(datapanel, "Open file");

				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fdia.getSelectedFile();
					edgesFile = file.getAbsolutePath();
				}
			} else if(label.equals(finishlabel)) {
				statusbar.setText(" " + label + " button clicked");
				insertframe.setVisible(false);
								
				/*Please wait frame*/
				pleasewaitframe = new JFrame();
				pleasewaitframe.setLayout(null);
				
				JLabel pleasewaitlabel = new JLabel();
				pleasewaitlabel.setText("Please Wait...");
				pleasewaitlabel.setBounds(10, 10, 100, 40);
				
				pleasewaitframe.add(pleasewaitlabel);
				pleasewaitframe.pack();
				pleasewaitframe.setTitle("Please Wait...");
				pleasewaitframe.setSize(400, 400);
				pleasewaitframe.setLocationRelativeTo(null); 
				pleasewaitframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				pleasewaitframe.setVisible(true);

				topology = new Topology(topologyFile, delisMap.get(topologyDel), topologyHEADER);
				allEdges = topology.getAllNewEdges();
				uniqueNodes = topology.getUniqueNodes();
				indexedEdges = comboEdges( allEdges );
				records = new Records(recordsFile, delisMap.get(edgesDel), true, recordsIDS);

				if (nodesFile != null) {
					createNodesInfoTable(nodesFile, delisMap.get(nodesDel));
				}

				if (edgesFile != null) {
					createRecordsInfoTable(edgesFile, delisMap.get(edgesDel));
				}
								
				pleasewaitframe.setVisible(false);//end 'please wait' frame
				secondUI();
			}
		}
	}
	private class ItemListener1 implements ItemListener{
		@Override
		public void itemStateChanged(ItemEvent e) {

			if (e.getStateChange() == ItemEvent.SELECTED) {
				JComboBox<String> o = (JComboBox<String>) e.getSource();
				String name = o.getName();
				if (name.equals(topologyDeli)) {
					topologyDel = e.getItem().toString();
				} else if (name.equals(recordsDeli)) {
					recordsDel = e.getItem().toString();
				} else if (name.equals(nodesDeli)) {
					nodesDel = e.getItem().toString();
				} else if (name.equals(edgesDeli)) {
					edgesDel = e.getItem().toString();
				}
			}
		}
	}
	private void insertData() {
		insertframe = new JFrame();
		insertframe.setLayout(null);
		
		delisMap.put(COMMA, ",");
		delisMap.put(SEMICOLON, ";");
		delisMap.put(VERTICAL_BAR, "\\|");
		
		JLabel topologylabel = new JLabel();
		topologylabel.setText("Topology File:");
		topologylabel.setBounds(10, 5, 100, 15);
		JLabel recordslabel = new JLabel();
		recordslabel.setText("Records File:");
		recordslabel.setBounds(10, 75, 100, 15);
		JLabel optional = new JLabel();
		optional.setText("(Optional Files:)");
		optional.setBounds(10, 170, 100, 15);
		JLabel nodeslabel = new JLabel();
		nodeslabel.setText("Nodes Info File:");
		nodeslabel.setBounds(10, 190, 100, 15);
		JLabel edgeslabel = new JLabel();
		edgeslabel.setText("Edges Info File:");
		edgeslabel.setBounds(10, 255, 100, 15);
		
		JCheckBox hasHeaderCB0 = new JCheckBox("Has Header?", false);
		hasHeaderCB0.setBounds(300, 30, 100, 25);
		JCheckBox hasHeaderCB1 = new JCheckBox("Has Header?", true);
		JCheckBox hasRecId0 = new JCheckBox("Has Rec IDs?", false);
		hasRecId0.setBounds(300, 100, 100, 25);
		hasHeaderCB0.setName(topologyHeader);
		hasHeaderCB1.setName(recordsHeader);
		hasRecId0.setName(recordsIds);
		hasHeaderCB0.addItemListener(new ItemListener2());
		hasHeaderCB1.addItemListener(new ItemListener2());
		hasRecId0.addItemListener(new ItemListener2());

		JComboBox<String> box0 = new JComboBox<>(delis);
		box0.setName(topologyDeli);
		box0.addItemListener(new ItemListener1());
		box0.setBounds(180, 30, 110, 25);

		JComboBox<String> box1 = new JComboBox<>(delis);
		box1.setName(recordsDeli);
		box1.setBounds(180, 100, 110, 25);
		box1.addItemListener(new ItemListener1());

		JComboBox<String> box2 = new JComboBox<>(delis);
		box2.setName(nodesDeli);
		box2.setBounds(180, 210, 110, 25);
		box2.addItemListener(new ItemListener1());

		JComboBox<String> box3 = new JComboBox<>(delis);
		box3.setName(edgesDeli);
		box3.setBounds(180, 275, 110, 25);
		box3.addItemListener(new ItemListener1());
		
		statusbar = new JLabel("ZetCode");
		statusbar.setBorder(BorderFactory.createEtchedBorder());

		ButtonListener butlist = new ButtonListener();

		JButton topologyButton = new JButton(topologyLabel);
		topologyButton.setBounds(10, 30, 160, 25);
		topologyButton.addActionListener(butlist);

		JButton recordsButton = new JButton(recordsLabel);
		recordsButton.setBounds(10, 100, 160, 25);
		recordsButton.addActionListener(butlist);

		JButton nodesButton = new JButton(nodesLabel);
		nodesButton.setBounds(10, 210, 160, 25);
		nodesButton.addActionListener(butlist);

		JButton edgesButton = new JButton(edgesLabel);
		edgesButton.setBounds(10, 275, 160, 25);
		edgesButton.addActionListener(butlist);
		
		JButton topohelpButton = new JButton(topohelp);
		topohelpButton.setBounds(420, 30, 120, 25);
		topohelpButton.addActionListener(butlist);
		JButton recohelpButton = new JButton(recohelp);
		recohelpButton.setBounds(420, 100, 120, 25);
		recohelpButton.addActionListener(butlist);
		JButton nodehelpButton = new JButton(nodehelp);
		nodehelpButton.setBounds(420, 210, 120, 25);
		nodehelpButton.addActionListener(butlist);
		JButton edgehelpButton = new JButton(edgehelp);
		edgehelpButton.setBounds(420, 275, 120, 25);
		edgehelpButton.addActionListener(butlist);				
		
		JButton finishButton = new JButton(finishlabel);
		finishButton.setBounds(420, 330, 120, 25);
		finishButton.addActionListener(butlist);
		finishButton.setBackground(Color.cyan);
		
		//topology panel
		insertframe.add(topologylabel);
		insertframe.add(topologyButton);
		insertframe.add(box0);
		insertframe.add(hasHeaderCB0);
		insertframe.add(topohelpButton);
		//records panel
		insertframe.add(recordslabel);
		insertframe.add(recordsButton);
		insertframe.add(box1);
		//insertframe.add(hasHeaderCB1);
		insertframe.add(hasRecId0);
		insertframe.add(recohelpButton);
		//optional panel
		insertframe.add(optional);
		//nodes info panel
		insertframe.add(nodeslabel);
		insertframe.add(nodesButton);
		insertframe.add(box2);
		insertframe.add(nodehelpButton);
		//edges info panel
		insertframe.add(edgeslabel);
		insertframe.add(edgesButton);
		insertframe.add(box3);
		insertframe.add(edgehelpButton);
		//finish panel
		insertframe.add(finishButton);
		
		insertframe.add(statusbar, BorderLayout.SOUTH);

		insertframe.pack();
		insertframe.setTitle("Import Data");
		insertframe.setSize(580, 420);
		insertframe.setLocationRelativeTo(null);
		insertframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		insertframe.setVisible(true);
	}
	private class ItemListener2 implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			
			if (e.getStateChange() == ItemEvent.SELECTED) {
				JCheckBox jo = (JCheckBox) e.getSource();
				String check = jo.getName();
				if (check.equals(topologyHeader)) {
					topologyHEADER = true;
				} else if (check.equals(recordsHeader)) {
					recordsHEADER = true;
				} else if (check.equals(recordsIds)) {
					recordsIDS = true;
				}
			
			}else if(e.getStateChange() == ItemEvent.DESELECTED){
				JCheckBox jo = (JCheckBox) e.getSource();
				String check = jo.getName();
				if (check.equals(topologyHeader)) {
					topologyHEADER = false;
				} else if (check.equals(recordsHeader)) {
					recordsHEADER = false;
				} else if (check.equals(recordsIds)) {
					recordsIDS = false;
				}
			}
			
		}

	}
	
	private class ActionListener1 implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			uniqueedges 			= new HashSet<>();
			List<String> selected 	= new ArrayList<>(ShowGraph.selectedEdges);
			uniqueedges.addAll(selected);
			
			// get all record ids for given query:
			try {
				dbInit();
				BVedges 	= createBVEdges(uniqueedges);
				allrecids 	= records.getAllIdsForUserQuery(BVedges);
				dbClose();

			} catch (ClassNotFoundException | SQLException e1) {
				e1.printStackTrace();
			}

			if (allrecids == null) {
				ShowGraph.selectedEdges = new HashSet<>();
				createPopupMenu();
				initUI();
			} else if (selected.size() == 0){
				createPopupMenu2();
				initUI();
			} else {
				enableRecordIds = true;				
				secondUI();				
			}
		}
	}
	
	private String[] createBVEdges(Set<String> edges){
		String[] bvedges = new String[edges.size()];
		AtomicInteger i = new AtomicInteger(0);
		edges.forEach(e->{
			bvedges[i.get()] = "BV"+e;
			i.getAndIncrement();
		});
		return bvedges;
	}
	private String[] createVEdges(Set<String> edges){
		String[] vedges = new String[edges.size()];
		AtomicInteger i = new AtomicInteger(0);
		edges.forEach(e->{
			vedges[i.get()] = "V"+e;
			i.getAndIncrement();
		});
		return vedges;
	}

	private class ActionListener2 implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);	
		}
		
	}
	private class ItemStateChanged1 implements ItemListener{
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				selectedFun = e.getItem().toString();
	        }
		}
	}
	private class ActionListener5 implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			enableRecordIds = false;
			enableRecord    = false;
			secondUI();
		}
		
	}
	private class ActionListener4 implements ActionListener{
		@Override
		public void actionPerformed (ActionEvent e) {
			try {
				dbInit();				
				String[] Vedges 		= createVEdges(uniqueedges);
				Double[] values 		= records.getQueryValuesForRecId(Vedges, BVedges, selectedRecId);
				funresedges 			= Functions.chooseFun(selectedFun, values);
				allqueryvalues 			= new LinkedHashMap<>();
				allqueryvalues 			= records.getAllQueryValuesForRecId(selectedRecId);
				Double[] allv 			= new Double[allqueryvalues.size()];
				AtomicInteger i 		= new AtomicInteger(0);
				allqueryvalues.forEach((c,v)->{
					allv[i.get()] = v;
					i.getAndIncrement();
				});
				funresalledges 			= Functions.chooseFun(selectedFun, allv);
				recEdges 				= splitVEdges(allqueryvalues);
				recEdges 				= linkUnlinkedNodes(recEdges);
				dbClose();
				ShowGraph.selectedEdges = new HashSet<>();
				enableRecord 			= true;
				
			} catch (ClassNotFoundException | SQLException e1) {
				e1.printStackTrace();
			}			
			secondUI();
		}		
	}
	private String[][] linkUnlinkedNodes(String[][] recEdges){
		List<String[]> newRecEdges = new LinkedList<>();
		List<String> temp = new LinkedList<>();
		for(int i=0; i<recEdges.length; i++){
			for(int j=0; j<recEdges[i].length; j++){
				temp.add(recEdges[i][j]);
			}
		}
		for(int k=0; k<temp.size()-1; k++){
			String[] couple = new String[2];
			couple[0] = temp.get(k);
			couple[1] = temp.get(k+1);
			newRecEdges.add(couple);
		}		
		return newRecEdges.toArray(new String[0][]);		
	}

	private String[][] splitVEdges(Map<String, Double> aqv){
		String[][] recEdges = new String[aqv.size()][2];
		AtomicInteger i = new AtomicInteger(0);
		aqv.forEach((col,value)->{
			String[] split = col.split("to");
			recEdges[i.get()][0] = split[0].substring(1);
			recEdges[i.get()][1] = split[1];
			i.getAndIncrement();				
		});
		return recEdges;
	}

	private void createPopupMenu() {
		JFrame pop = new JFrame();
        pop.setLayout(new BorderLayout());
        
        JLabel nullmessage = new JLabel("<html><b> NO SUCH EDGES COMBINATION </b></html>");
        JButton retur = new JButton("OK");
        retur.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pop.setVisible(false);
			}
		});
        
        JPanel mes = new JPanel(new BorderLayout());
        mes.add(nullmessage, BorderLayout.CENTER);
        mes.add(retur, BorderLayout.SOUTH);
        
        pop.getContentPane().add(mes, BorderLayout.CENTER);
        pop.setTitle("JPopupMenu");
        pop.setSize(250,120);
        pop.setLocationRelativeTo(null);
        pop.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pop.setVisible(true);
        SwingUtilities.invokeLater(()->{
        	pop.getContentPane().validate();
        	pop.getContentPane().repaint();
        });
    }
	private void createPopupMenu2() {
		JFrame pop = new JFrame();
        pop.setLayout(new BorderLayout());
        
        JLabel nullmessage = new JLabel("<html><b> PLEASE SELECT ANY EDGES </b></html>");
        JButton retur = new JButton("OK");
        retur.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pop.setVisible(false);
			}
		});
        
        JPanel mes = new JPanel(new BorderLayout());
        mes.add(nullmessage, BorderLayout.CENTER);
        mes.add(retur, BorderLayout.SOUTH);
        
        pop.getContentPane().add(mes, BorderLayout.CENTER);
        pop.setTitle("JPopupMenu");
        pop.setSize(250,120);
        pop.setLocationRelativeTo(null);
        pop.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pop.setVisible(true);
        SwingUtilities.invokeLater(()->{
        	pop.getContentPane().validate();
        	pop.getContentPane().repaint();
        });
    }
		
	private void createNodesInfoTable(String fileName, String del){
		try{
			String[][] ni = readCSV( fileName, del, true );

			nodesinfoheaders = getHeaders(fileName, del).toArray(new String[0]);
			ShowGraph.nodesInfoHeaders = nodesinfoheaders;

			Map<String, String> columnProps =
				IntStream.range(0, nodesinfoheaders.length)
						 .boxed()
						 .collect(Collectors.toMap(i-> i == 0 ? ID : nodesinfoheaders[i],
												   i-> i == 0 ? INT : VARCHAR+"(250)"));

			List<Integer> nominals = IntStream.range(1, ni[0].length).boxed().collect(Collectors.toList());

			dbInit();
			dropTable(NODESINFO);
			createTable(NODESINFO, ID, columnProps);
			insertValuesToTable(NODESINFO, nodesinfoheaders, ni, nominals);
			dbClose();

			if (Files.exists(Paths.get("metadata/nodesinfoheaders.txt"))) {
				FileUtil.deleteFile( "metadata/nodesinfoheaders.txt" );
			}

			Path file = Paths.get( "metadata/nodesinfoheaders.txt" );
			Files.write(file, Arrays.asList(nodesinfoheaders) );

		} catch (Throwable t) {
			try {
				dbInit();
				dropTable(NODESINFO);
				dbClose();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private void createRecordsInfoTable(String fileName, String del){
		try {
			String[][] ri = readCSV(fileName, del, true);

			recordsinfoheaders = getHeaders( fileName, del ).toArray(new String[0]);
			ShowGraph.recordsInfoHeaders = recordsinfoheaders;

			Map<String, String> columnProps =
					IntStream.range(0, recordsinfoheaders.length)
							.boxed()
							.collect(Collectors.toMap(i-> i == 0 ? ID : recordsinfoheaders[i],
									i-> i == 0 ? INT : VARCHAR+"(250)"));
			List<Integer> nominals = IntStream.range(1, ri[0].length).boxed().collect(Collectors.toList());

			dbInit();
			dropTable(RECORDSINFO);
			createTable(RECORDSINFO, ID, columnProps);
			insertValuesToTable(RECORDSINFO, recordsinfoheaders, ri, nominals);
			dbClose();

			if(Files.exists( Paths.get("metadata/recordsinfoheaders.txt" ) )){
				FileUtil.deleteFile( "metadata/recordsinfoheaders.txt" );
			}

			Path file = Paths.get( "metadata/recordsinfoheaders.txt" );
			Files.write(file, Arrays.asList(recordsinfoheaders) );

		} catch (Throwable t) {
			try {
				dbInit();
				dropTable(RECORDSINFO);
				dbClose();
			} catch (Exception e){
				e.printStackTrace();
			}
			t.printStackTrace();
		}
	}
	
}
