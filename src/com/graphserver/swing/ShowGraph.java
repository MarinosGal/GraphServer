package com.graphserver.swing;
	
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import com.graphserver.module.monetdb.dao.MonetDbDao;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class ShowGraph {
      
	private static DirectedSparseGraph<String, Number> insidegraph;	 
	private static final String    IMAGE_LOCATION     = "/images/Sandstone.jpg";
	private static final ImageIcon ICON               =  new ImageIcon(ShowGraph.class.getResource(IMAGE_LOCATION));
	private static final String    NODES_INFO_QUERY   = "select * from "+MonetDbDao.NODESINFO+" where "+MonetDbDao.ID+"=";
	private static final String    RECORDS_INFO_QUERY = "select * from "+MonetDbDao.RECORDSINFO+" where "+MonetDbDao.ID+"=";
	
	public static String[] 				nodesInfoHeaders   	= null;
	public static String[] 				recordsInfoHeaders 	= null;
	public static String   				recordId			= null;
	public static boolean  				record 				= false;
	public static String[][]	 		recordsEdges		= null;
	public static boolean				noRecordId			= true;

	public static String[] 				edgeSelected;
	public static Set<String> 			selectedEdges;

	public static JPanel initVV(String graphName,DirectedSparseGraph<String, Number> graph){
		edgeSelected   = new String[2];
		selectedEdges  = new HashSet<>();
				  
		VisualizationViewer<String,Number> vv = new VisualizationViewer<String,Number>(new FRLayout<String,Number>(graph));
        insidegraph = graph;
		vv.addPreRenderPaintable(new VisualizationViewer.Paintable(){
			public void paint(Graphics g) {
				 Dimension d = vv.getSize();
				 g.drawImage(ICON.getImage(),0,0,d.width,d.height,vv);
			}
			public boolean useTransform() { return false; }
		});

        vv.addPostRenderPaintable(new VisualizationViewer.Paintable(){
           int x;
           int y;
           Font font;
           
           public void paint(Graphics g) {
                 if (font == null) {
                     font = new Font(g.getFont().getName(), Font.BOLD, 25);
                     x = 20;
                     y = 30;
                 }
                 g.setFont(font);
                 Color oldColor = g.getColor();
                 g.setColor(Color.lightGray);
                 g.drawString(graphName, x, y);
                 g.setColor(oldColor);
             }
            public boolean useTransform() {
                return false;
            }
        });
 
        vv.addGraphMouseListener(new TestGraphMouseListener<>());
        vv.getRenderer().setVertexRenderer(
        		new GradientVertexRenderer<>(
        				Color.white, Color.red,
         				Color.white, Color.green,
         				vv.getPickedVertexState(),
         				false));

        //edge picking
        final PickedState<Number> pickedState = vv.getPickedEdgeState();
        pickedState.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object subject = e.getItem();
				if (subject instanceof Number) {
					Number edge = (Number)subject;
					if (pickedState.isPicked(edge)) {
						System.out.println("Edge "+edge+" is now selected ["+insidegraph.getSource(edge)+","+insidegraph.getDest(edge)+"]");
						selectedEdges.add(insidegraph.getSource(edge)+"to"+insidegraph.getDest(edge));
						edgeSelected[0] = insidegraph.getSource(edge);
						edgeSelected[1] = insidegraph.getDest(edge);
						vv.getRenderContext().setLabelOffset(0);
						vv.getRenderContext().setEdgeDrawPaintTransformer(edgepaint);
					} else {
						vv.getRenderContext().setLabelOffset(0);
						vv.getRenderContext().setEdgeDrawPaintTransformer(recordsEdgesPaint);					
					}
				}				
			}
		});
        vv.getRenderContext().setEdgeDrawPaintTransformer(recordsEdgesPaint);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStroke);
        vv.getRenderContext().setLabelOffset(0);
        
        //vv.getRenderContext().setEdgeDrawPaintTransformer(new ConstantTransformer(Color.lightGray));
        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
        vv.getRenderContext().setArrowDrawPaintTransformer(new ConstantTransformer(Color.lightGray));
         
        // add my listeners for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<>());
        vv.setEdgeToolTipTransformer(new Transformer<Number,String>() {
			public String transform(Number edge) {
 				return "E"+graph.getEndpoints(edge).toString();
 			}});
         
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>());
        vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
        vv.setForeground(Color.lightGray);
      
        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<String,Number>();
        graphMouse.setMode(Mode.PICKING);
        vv.setGraphMouse(graphMouse);
         
        vv.addKeyListener(graphMouse.getModeKeyListener());
        vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");
        
        final ScalingControl scaler = new CrossoverScalingControl();
        
        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 scaler.scale(vv, 1.1f, vv.getCenter());
             }
         });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
 
        JButton reset = new JButton("reset");
        reset.addActionListener(new ActionListener() {	
        	public void actionPerformed(ActionEvent e) {
				vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setToIdentity();
				vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();
 			}});
              
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		vv.setVisible(false);
        	}
        });
        
        JButton showButton = new JButton("Show");
        showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				vv.setVisible(true);
			}
		});

        JButton recordsinfo = new JButton("Record's Info");
        recordsinfo.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if(recordsInfoHeaders!=null){
        			String[][] recordsInfoResult = null;
        			try {
						MonetDbDao.dbInit();
	        			recordsInfoResult = MonetDbDao.getQueryResult(RECORDS_INFO_QUERY +recordId+";", recordsInfoHeaders);
	        			MonetDbDao.dbClose();
					} catch (ClassNotFoundException | SQLException e1) {
						e1.printStackTrace();
					}
        			JFrame pop = new JFrame();
        			StringBuilder rir = new StringBuilder("<html><b>Record's info:</b>");
        			for(int i=0; i<recordsInfoResult.length; i++){
        				rir.append("<br>");
        				for(int j=0; j<recordsInfoResult[0].length; j++){
        					rir.append(recordsInfoHeaders[j]+": "+recordsInfoResult[i][j]);
        					rir.append("<br>");
        				}
        			}
        			rir.append("<br></html>");
        			//System.out.println(rir.toString());
        			JLabel nullmessage = new JLabel( rir.toString() );
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
        			pop.setTitle("Record's Information");
        			pop.setSize(300, 250);
        			pop.setLocationRelativeTo(null);
        			pop.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        			pop.setVisible(true);
        			SwingUtilities.invokeLater(()->{
        				pop.getContentPane().validate();
        				pop.getContentPane().repaint();
        			});
        		}else{
        			System.out.println("No available information!");
        		}
        	}
        });
    	JPanel controls = new JPanel(new FlowLayout());
        controls.add(plus);
        controls.add(minus);
        controls.add(reset);
        controls.add(close);
        controls.add(showButton);
        if(recordsInfoHeaders!=null){
        	controls.add(recordsinfo);
        }
        JPanel bottom = new JPanel();
        bottom.add(controls, new FlowLayout());
        
        JPanel graphpanel = new JPanel(new BorderLayout());
        graphpanel.add(vv, BorderLayout.CENTER);
        graphpanel.add(bottom, BorderLayout.SOUTH);
                
      return graphpanel;
	}
	private static Transformer<Number, Paint> recordsEdgesPaint = new Transformer<Number, Paint>() {
		@Override
		public Paint transform(Number s) {
        	final String source = insidegraph.getSource(s);
        	final String dest   = insidegraph.getDest(s);
			if (record) {
        		for (int i = 0; i < recordsEdges.length; i++) {
        			if (source.equals(recordsEdges[i][0]) &&
        			    dest.equals(recordsEdges[i][1])) {
        				return Color.MAGENTA;
        			}
        		}
        	}
			return Color.lightGray;
		}
	};
    private static Transformer<Number, Paint> edgepaint = new Transformer<Number, Paint>() {
        @Override
    	public Paint transform(Number s) {
        	final String source = insidegraph.getSource(s);
        	final String dest   = insidegraph.getDest(s);
        	if(source!=null && dest!=null && source.equals(edgeSelected[0]) && dest.equals(edgeSelected[1]) && noRecordId){
        		return Color.green;
        	}        	
            return Color.lightGray;
        }
    };
    private static Transformer<Number, Stroke> edgeStroke = new Transformer<Number, Stroke>() {
        float dash[] = { 0.1f };
        public Stroke transform(Number s) {
            return new BasicStroke(	2.0f, 
            						BasicStroke.CAP_ROUND,
            						BasicStroke.JOIN_MITER, 
            						10.0f, 
            						dash, 
            						0.0f);
        }
    };
   	
	/**
	 * A nested class to demo the GraphMouseListener finding the right vertices
	 * after zoom/pan
	 */
	static class TestGraphMouseListener<V> implements GraphMouseListener<V> {
		
		public void graphClicked(V v, MouseEvent me) {
			System.out.println("Vertex " + v + " was clicked at (" + me.getX() + "," + me.getY() + ")");
			if (nodesInfoHeaders != null) {
				System.out.println("Nodes info information exists.");
				String[][] nodeInfoResult = null;
				try {
					MonetDbDao.dbInit();
					nodeInfoResult = MonetDbDao.getQueryResult(NODES_INFO_QUERY + v + ";", nodesInfoHeaders);
					MonetDbDao.dbClose();
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}

				JFrame pop = new JFrame();
				pop.setLayout(new BorderLayout());
				StringBuilder nir = new StringBuilder("<html><b>Node's info:</b>");
				nir.append("<br>");
				for (int j = 0; j < nodeInfoResult[0].length; j++) {
					nir.append(nodesInfoHeaders[j] + " : " + nodeInfoResult[0][j]);
					nir.append("<br>");
				}
				nir.append("<br></html>");
				//System.out.println(nir.toString());
				JLabel nullmessage = new JLabel(nir.toString());// use html
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

				pop.getContentPane().add(nullmessage, BorderLayout.CENTER);
				pop.getContentPane().add(retur, BorderLayout.SOUTH);
				pop.setTitle("Node's Information");
				pop.setSize(300, 250);
				pop.setLocationRelativeTo(null);
				pop.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				pop.setVisible(true);
				SwingUtilities.invokeLater(() -> {
					pop.getContentPane().validate();
					pop.getContentPane().repaint();
				});
			}
		}

		public void graphPressed(V v, MouseEvent me) {
			
		}

		public void graphReleased(V v, MouseEvent me) {
			
		}
	}

}
