package es.toni.crytpoMain;

import java.awt.event.ItemEvent;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import es.toni.crytpoMain.base64.Base64;
import es.toni.crytpoMain.hash.Hash;
import es.toni.crytpoMain.symmetric.AlgSimetrico;
import es.toni.crytpoMain.utils.Constantes;


/**
 * @author Toni Ferreiro
 */
public class CryptoMain extends javax.swing.JFrame{

	private static final long serialVersionUID = 1L;
                  
    private javax.swing.JComboBox<String> algoritmo;
    private javax.swing.JComboBox<String> cifrado;
    private javax.swing.JTextField clave;
    private javax.swing.JTextArea mensaje;
    private javax.swing.JTextArea resultado;
    private javax.swing.JTextField vector;

    public CryptoMain() {
        initComponents();
    	setLocationRelativeTo(null);
    }

    private void initComponents() {    	

        final javax.swing.JLabel algoritmoLabel;
        final javax.swing.JLabel cifradoLabel;
        final javax.swing.JLabel claveLabel;
        final javax.swing.JScrollPane scrollMensaje;
        final javax.swing.JScrollPane scrollResultado;
        final javax.swing.JSeparator separadorVerticalCentral;
        final javax.swing.JButton limpiarButton;
        final javax.swing.JLabel mensajeLabel;
        final javax.swing.JLabel resultadoLabel;
        final javax.swing.JButton salirButton;
        final javax.swing.JButton cifrarButton;
        final javax.swing.JButton descifrarButton;
        final javax.swing.JLabel vectorLabel;
        final javax.swing.JLabel infoLabel;

        cifrarButton = new javax.swing.JButton();
        descifrarButton = new javax.swing.JButton();
        limpiarButton = new javax.swing.JButton();
        cifradoLabel = new javax.swing.JLabel();
        algoritmoLabel = new javax.swing.JLabel();
        claveLabel = new javax.swing.JLabel();
        vectorLabel = new javax.swing.JLabel();
        mensajeLabel = new javax.swing.JLabel();
        resultadoLabel = new javax.swing.JLabel();
        cifrado = new javax.swing.JComboBox<>();
        algoritmo = new javax.swing.JComboBox<>();
        clave = new javax.swing.JTextField();
        vector = new javax.swing.JTextField();
        scrollMensaje = new javax.swing.JScrollPane();
        mensaje = new javax.swing.JTextArea();
        scrollResultado = new javax.swing.JScrollPane();
        resultado = new javax.swing.JTextArea();
        separadorVerticalCentral = new javax.swing.JSeparator();
        salirButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        cifrarButton.setText("Cifrar");
        cifrarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	cifrarButtonActionPerformed(evt);
            }
        });

        descifrarButton.setText("Descifrar");
        descifrarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descifrarButtonActionPerformed(evt);
            }
        });

        limpiarButton.setText("Limpiar");
        limpiarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpiarButtonActionPerformed(evt);
            }
        });
        salirButton.setText("Salir");
        salirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salirButtonActionPerformed(evt);
            }
        });

        cifradoLabel.setText("Cifrado");
        
        cifrado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "","Base64", "Hash", "Simetrico", "Asimetrico - NA", "pem - NA"}));
        cifrado.addItemListener(new java.awt.event.ItemListener(){
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

        separadorVerticalCentral.setOrientation(javax.swing.SwingConstants.VERTICAL);

        infoLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        infoLabel.setText("Libreria externa: bcprov-jdk15on-165");
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cifrarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(algoritmoLabel)
                                    .addComponent(cifradoLabel)
                                    .addComponent(claveLabel)
                                    .addComponent(vectorLabel))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cifrado, 0, 223, Short.MAX_VALUE)
                                    .addComponent(algoritmo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(clave, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                                    .addComponent(vector)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mensajeLabel)
                                    .addComponent(scrollMensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(10, 10, 10)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(limpiarButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(salirButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(separadorVerticalCentral, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(resultadoLabel)
                                    .addComponent(scrollResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(26, 26, 26))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descifrarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(infoLabel)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cifrarButton)
                            .addComponent(descifrarButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(infoLabel)))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(cifradoLabel)
                                    .addComponent(cifrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(resultadoLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(algoritmoLabel)
                                    .addComponent(algoritmo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(claveLabel)
                                    .addComponent(clave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(vectorLabel)
                                    .addComponent(vector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addComponent(mensajeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollMensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(scrollResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separadorVerticalCentral, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(salirButton)
                    .addComponent(limpiarButton))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                             

    /**
     * 
     * @param evt
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws UnsupportedEncodingException 
     * @throws InvalidKeySpecException 
     */
    private void cifrarButtonActionPerformed(java.awt.event.ActionEvent evt){                                               
        switch(cifrado.getSelectedIndex()){
	        case 0:
	        	resultado.setText(Constantes.DATOS_INCORRECTOS);
	        case 1: 
	        	resultado.setText(Base64.cifrarBase64(algoritmo.getSelectedIndex(),mensaje.getText()));
	            clave.setText("");
	            vector.setText("");
	        	break;
	        case 2:
	        	resultado.setText(Hash.cifrarHash(algoritmo.getSelectedIndex(),mensaje.getText()));
	            clave.setText("");
	            vector.setText("");
	        	break;
	        case 3:
	        	resultado.setText(AlgSimetrico.cifrarSimetrico(algoritmo.getSelectedIndex(),mensaje.getText(),clave.getText(),vector.getText()));
	        	if (algoritmo.getSelectedIndex() != 0) {
	                vector.setText("");
	        	}
        	break;
        default: resultado.setText(Constantes.NO_APLICA);
        }
    }                                              

    /**
     * 
     * @param evt
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    private void descifrarButtonActionPerformed(java.awt.event.ActionEvent evt){                                                  
    	switch(cifrado.getSelectedIndex()){
        case 0:
        	resultado.setText(Constantes.DATOS_INCORRECTOS);
        case 1: 
        	resultado.setText(Base64.descifrarBase64(algoritmo.getSelectedIndex(),mensaje.getText()));
            clave.setText("");
            vector.setText("");
        	break;
        case 2:
        	resultado.setText(Constantes.ERROR_DESCIFRAR_HASH);
            clave.setText("");
            vector.setText("");
        	break;
        case 3:
        	resultado.setText(AlgSimetrico.descifrarSimetrico(algoritmo.getSelectedIndex(),mensaje.getText(),clave.getText(),vector.getText()));
        	if (algoritmo.getSelectedIndex() != 0) {
                vector.setText("");
        	}
        	break;
        default: resultado.setText(Constantes.NO_APLICA);
        }
    }      
    
    /**
     * 
     * @param evt
     */
    private void limpiarButtonActionPerformed(java.awt.event.ActionEvent evt) { 
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
    private void salirButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        this.dispose();
    }  
    
    /**
     * 
     * @param evt
     */
    private void changeComboAlgoritmo(java.awt.event.ItemEvent evt){
    	if (java.awt.event.ItemEvent.DESELECTED == evt.getStateChange()){
    	} else if (java.awt.event.ItemEvent.SELECTED == evt.getStateChange()){
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
        case "Base64": 
        	algoritmo.addItem("java.util");
        	algoritmo.addItem("org.bouncycastle.util.encoders");
        	break;
        case "Hash":
        	algoritmo.addItem("java.security.MessageDigest MD5");
        	algoritmo.addItem("java.security.MessageDigest SHA1");
        	break;
        case "Simetrico":
        	algoritmo.addItem("AES/CBC/PKCS5Padding");
        	algoritmo.addItem("AES/ECB/PKCS7Padding BC");
        	algoritmo.addItem("DES/CBC/PKCS5Padding");
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