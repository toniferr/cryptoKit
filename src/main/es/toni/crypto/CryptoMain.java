package es.toni.crypto;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import es.toni.crypto.base64.Base64;
import es.toni.crypto.hash.Hash;
import es.toni.crypto.hex.HexCode;
import es.toni.crypto.symmetric.AlgSimetrico;
import es.toni.crypto.utils.Constantes;


/**
 * @author Toni Ferreiro
 */
public class CryptoMain extends JFrame{

	private static final long serialVersionUID = 1L;
                  
    private JComboBox<String> algoritmo;
    private JComboBox<String> cifrado;
    private JTextField clave;
    private JTextArea mensaje;
    private JTextArea resultado;
    private JTextField vector;

    public CryptoMain() {
        initComponents();
    	setLocationRelativeTo(null);
    }

    private void initComponents() {    	

        final JLabel algoritmoLabel = new JLabel();
        final JLabel cifradoLabel = new JLabel();
        final JLabel claveLabel = new JLabel();
        final JScrollPane scrollMensaje = new JScrollPane();
        final JScrollPane scrollResultado = new JScrollPane();
        final JSeparator separadorVerticalCentral = new JSeparator();
        final JButton limpiarButton = new JButton();
        final JLabel mensajeLabel = new JLabel();
        final JLabel resultadoLabel = new JLabel();
        final JButton salirButton = new JButton();
        final JButton cifrarButton = new JButton();
        final JButton descifrarButton = new JButton();
        final JLabel vectorLabel = new JLabel();
        final JLabel infoLabel = new JLabel();

        cifrado = new JComboBox<>();
        algoritmo = new JComboBox<>();
        clave = new JTextField();
        vector = new JTextField();
        mensaje = new JTextArea();
        resultado = new JTextArea();        

    	clave.setEditable(false);
    	vector.setEditable(false);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        cifrarButton.setText("Cifrar");
        cifrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	cifrarButtonActionPerformed(evt);
            }
        });

        descifrarButton.setText("Descifrar");
        descifrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                descifrarButtonActionPerformed(evt);
            }
        });

        limpiarButton.setText("Limpiar");
        limpiarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                limpiarButtonActionPerformed(evt);
            }
        });
        salirButton.setText("Salir");
        salirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                salirButtonActionPerformed(evt);
            }
        });

        cifradoLabel.setText("Cifrado");
        
        cifrado.setModel(new DefaultComboBoxModel<>(new String[] { "","Hexadecimal", "Base64", "Hash", "Simetrico"}));
        cifrado.addItemListener((ItemListener) new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				changeComboAlgoritmo(e);
			}
        });

        algoritmoLabel.setText("Algoritmo");

        claveLabel.setText("Clave/Semilla");

        vectorLabel.setText("Vector");

        mensajeLabel.setText("Mensaje");

        resultadoLabel.setText("Resultado");

        mensaje.setColumns(20);
        mensaje.setRows(5);
        scrollMensaje.setViewportView(mensaje);

        resultado.setColumns(20);
        resultado.setRows(5);
        scrollResultado.setViewportView(resultado);

        separadorVerticalCentral.setOrientation(SwingConstants.VERTICAL);

        infoLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        infoLabel.setText("Provider externo: bcprov-jdk15on-1.70");
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cifrarButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(algoritmoLabel)
                                    .addComponent(cifradoLabel)
                                    .addComponent(claveLabel)
                                    .addComponent(vectorLabel))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cifrado, 0, 223, Short.MAX_VALUE)
                                    .addComponent(algoritmo, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(clave, GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                                    .addComponent(vector)))
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(mensajeLabel)
                                    .addComponent(scrollMensaje, GroupLayout.PREFERRED_SIZE, 281, GroupLayout.PREFERRED_SIZE))))
                        .addGap(10, 10, 10)))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(limpiarButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(salirButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(separadorVerticalCentral, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(resultadoLabel)
                                    .addComponent(scrollResultado, GroupLayout.PREFERRED_SIZE, 402, GroupLayout.PREFERRED_SIZE))))
                        .addGap(26, 26, 26))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descifrarButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(infoLabel)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cifrarButton)
                            .addComponent(descifrarButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(infoLabel)))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(cifradoLabel)
                                    .addComponent(cifrado, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(resultadoLabel)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(algoritmoLabel)
                                    .addComponent(algoritmo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(claveLabel)
                                    .addComponent(clave, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(vectorLabel)
                                    .addComponent(vector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addComponent(mensajeLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollMensaje, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE))
                            .addComponent(scrollResultado, GroupLayout.PREFERRED_SIZE, 249, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separadorVerticalCentral, GroupLayout.PREFERRED_SIZE, 295, GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(salirButton)
                    .addComponent(limpiarButton))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                             

    /**
     * 
     * @param evt
     */
    private void cifrarButtonActionPerformed(ActionEvent evt){                                               
        switch(cifrado.getSelectedIndex()){
	        case 0:
	        	resultado.setText(Constantes.DATOS_INCORRECTOS);
	        	break;
	        case 1:			
	        	resultado.setText(HexCode.cifrarStringToHex(mensaje.getText()));
	        	break;
	        case 2: 
	        	resultado.setText(Base64.cifrarBase64(mensaje.getText()));
	        	break;
	        case 3:
	        	resultado.setText(Hash.cifrarHash(algoritmo.getSelectedIndex(),mensaje.getText()) +" (hexadecimal)");
	        	break;
	        case 4:
                String salida = "";
                if (algoritmo.getSelectedIndex() == 7){
                    salida = "base64";
                } else {
                    salida = "hexadecimal";
                }
	        	resultado.setText(AlgSimetrico.cifrarSimetrico(algoritmo.getSelectedIndex(),mensaje.getText(),clave.getText(),vector.getText()) +" ("+salida+")");
	        	break;
	        default: resultado.setText(Constantes.NO_APLICA);
        }
    }                                              

    /**
     * 
     * @param evt
     */
    private void descifrarButtonActionPerformed(ActionEvent evt){                                                  
    	switch(cifrado.getSelectedIndex()){
	        case 0:
	        	resultado.setText(Constantes.DATOS_INCORRECTOS);
	        	break;
	        case 1:	
	        	resultado.setText(HexCode.descifrarHexToString(mensaje.getText()));
	        	break;
	        case 2: 
	        	resultado.setText(Base64.descifrarBase64(mensaje.getText()));
	        	break;
	        case 3:
	        	resultado.setText(Constantes.ERROR_DESCIFRAR_HASH);
	        	break;
	        case 4:
	        	resultado.setText(AlgSimetrico.descifrarSimetrico(algoritmo.getSelectedIndex(),mensaje.getText(),clave.getText(),vector.getText()));
        	break;
	        default: resultado.setText(Constantes.NO_APLICA);
        }
    }      
    
    /**
     * 
     * @param evt
     */
    private void limpiarButtonActionPerformed(ActionEvent evt) { 
        cifrado.setSelectedIndex(0);
        clave.setText("");
        vector.setText("");
        mensaje.setText("");
        resultado.setText("");
        
    } 

    /**
     * 
     * @param evt
     */
    private void salirButtonActionPerformed(ActionEvent evt) {                                            
        this.dispose();
    }  
    
    /**
     * 
     * @param evt
     */
    private void changeComboAlgoritmo(ItemEvent evt){
    	if (ItemEvent.DESELECTED == evt.getStateChange()){
    	} else if (ItemEvent.SELECTED == evt.getStateChange()){
    		anadirItemsComboAlgoritmo(evt.getItem().toString());
    	}
    }
    
    /**
     * 
     * @param text
     */
    private void anadirItemsComboAlgoritmo(String text){
    	algoritmo.removeAllItems();
    	switch(text){
	        case "Hexadecimal": 
	            clave.setText("");
	            vector.setText("");
	        	clave.setEditable(false);
	        	vector.setEditable(false);	        	
	        	algoritmo.addItem("org.bouncycastle.util.encoders.Hex");
	        	break;
	        case "Base64": 
	            clave.setText("");
	            vector.setText("");
	        	clave.setEditable(false);
	        	vector.setEditable(false);		        	
	        	algoritmo.addItem("java.util.Base64");
	        	break;
	        case "Hash":
	            clave.setText("");
	            vector.setText("");
	        	clave.setEditable(false);
	        	vector.setEditable(false);		        	
	        	algoritmo.addItem("java.security.MessageDigest MD5");
	        	algoritmo.addItem("java.security.MessageDigest SHA1");
	        	algoritmo.addItem("java.security.MessageDigest SHA-256");
	        	algoritmo.addItem("java.security.MessageDigest SHA-512");
	        	algoritmo.addItem("org.bouncycastle.jcajce SHA3-256");
	        	algoritmo.addItem("org.bouncycastle.jcajce SHA3-512");
	        	break;
	        case "Simetrico":	
	            clave.setText("");
	            vector.setText("");
	        	clave.setEditable(true);
	        	vector.setEditable(true);	        	
	        	algoritmo.addItem("DES/CBC/PKCS5Padding");
	        	algoritmo.addItem("DES/ECB/PKCS5Padding");
	        	algoritmo.addItem("AES/CBC/PKCS7Padding BC");
	        	algoritmo.addItem("AES/ECB/PKCS7Padding BC");
	        	algoritmo.addItem("AES/GCM/NoPadding BC");
	        	algoritmo.addItem("Blowfish/CBC/PKCS5Padding BC");
	        	algoritmo.addItem("Blowfish/ECB/PKCS5Padding BC");
                algoritmo.addItem("PBEWithHMACSHA512AndAES_256 Jasypt");
	        	break;
        default: break;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CryptoMain().setVisible(true);
            }
        });
    }
    
    
}