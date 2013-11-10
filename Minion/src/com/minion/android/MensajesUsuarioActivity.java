package com.minion.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.desasic.whosfuckin.bd.ModeloWF;

import com.desasic.whosfuckin.service.MensajesService;
import com.desasic.whosfuckin.util.ClaveConfiguracion;
import com.desasic.whosfuckin.util.ComparadorMensaje;
import com.desasic.whosfuckin.util.Constantes;
import com.desasic.whosfuckin.util.ImageCacheUtil;
import com.desasic.whosfuckin.util.ImageUtil;
import com.desasic.whosfuckin.util.Utils;
import com.desasic.whosfuckin.util.WFLog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.whosfuckin.android.dto.RestMensaje;
import com.whosfuckin.android.dto.RestRespuestaMensaje;
import com.whosfuckin.android.dto.RestRespuestaUsuario;
import com.whosfuckin.android.dto.RestUsuario;

public class MensajesUsuarioActivity extends Activity {

	public static String ACTION_STRING = "whosfuckinAndroidMensajesUsuarioActivity";
	public static final int DIALOGO_CARGANDO_CONVERSACIONES = 1;
	public static final int DIALOGO_ELIMINANDO_MENSAJE = 2;
	public static final int DIALOGO_BLOQUEANDO_USUARIO = 3;
	public static final int DIALOGO_DESBLOQUEANDO_USUARIO = 4;
	public static final int DIALOGO_GUARDANDO_GALERIA = 5;
	private ProgressDialog pDialog = null;

	private static final int REQ_CODE_PICK_IMAGE = 555;
	private static final int REQ_CODE_TAKE_IMAGE = 556;
	private static final String TEMP_PHOTO_FILE = "tmppctmsg.wfjpg";

	private static final int IMAGEN_ALTURA_DEFECTO = 1024;
	private static final int IMAGEN_ANCHURA_DEFECTO = 1024;

	private MinionApplication app;


	private String usuarioMSG;
	private Button btnEnviar;
	private EditText editMensaje;

	private ListView lvwMensajes;
	private boolean nuevoMSG = false;

	private Hashtable<Long, MinionMensajeResponse> mensajesPendientes = new Hashtable<Long, MinionMensajeResponse>();
	private int idAvatar;
	private Integer idImagen;
	private TextView loginUsuario;
	private ImageView imgUsuario;
	// private LinearLayout llyUsuario;
	private ImageView imgBloquear;

	/**
	 * ID del último mensaje que hemos recibido del servidor mediante consulta (no notificacion)
	 */
	private Integer idUltimoMensajeConsolidado = null;

	private Timer autoUpdate = null;
	private int imagenAnchura;
	private int imagenAltura;


	private MensajesArrayAdapter mensajesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_chat);

		this.app = (MinionApplication) getApplication();

		// Carga de datos dependiendo de si se nos informa el picadero completo o solo el id
		Bundle bundle = this.getIntent().getExtras();
		usuarioMSG = bundle.getString(Constantes.PARAM_USUARIO_ID);
		idAvatar = bundle.getInt(Constantes.PARAM_ID_IMAGEN_USUARIO);
		idImagen = bundle.getInt(Constantes.PARAM_IMAGEN_USUARIO);

		try {
			if (app.getNuevosMensajes().size() > 0) {
				app.eliminarNuevosMensajes(usuarioMSG);
			}
		} catch (Exception e) {
			MnLog.e(this, "Error al eliminar los mensajes nuevos de la app: " + e.getMessage(), e);
		}

		lvwMensajes = (ListView) findViewById(R.id.msu_lvw_mensajes);
		lvwMensajes.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lvwMensajes.setStackFromBottom(true);
		mensajesAdapter = new MensajesArrayAdapter(this, R.layout.mensaje_list_layout, ultimosMensajes);
		lvwMensajes.setAdapter(mensajesAdapter);
		boolean pauseOnScroll = false; // or true
		boolean pauseOnFling = true; // or false
		PauseOnScrollListener listener = new PauseOnScrollListener(ImageLoader.getInstance(), pauseOnScroll,
				pauseOnFling);
		lvwMensajes.setOnScrollListener(listener);

		// scvMensajes = (ScrollView) findViewById(R.id.msu_scv_mensajes);
		// scvMensajes.fullScroll(View.FOCUS_DOWN);
		// llyMensajes = (LinearLayout) findViewById(R.id.msu_lly_mensajes);

		loginUsuario = (TextView) findViewById(R.id.msu_txt_login);
		imgUsuario = (ImageView) findViewById(R.id.msu_img_usuario);

		loginUsuario.setText(usuarioMSG);
		imgUsuario.setImageResource(Utils.obtenerIdImagenAvatarString(idAvatar));
		if (idImagen != null && idImagen.intValue() > 0) {
			// ImageCacheUtil.getCache().download(idImagen, imgUsuario, app.getUsuarioApp(),
			// R.drawable.img_usuario_whosfuckin, app);

			ImageLoader.getInstance().displayImage(ImageCacheUtil.obtenerURLImgUsuario(idImagen, this), imgUsuario,
					app.dioIMGUsuario);
		}

		// llyUsuario = (LinearLayout) findViewById(R.id.msu_lly_usuario);
		imgUsuario.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				verUsuario();
			}
		});

		loginUsuario.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				verUsuario();
			}
		});

		editMensaje = (EditText) findViewById(R.id.msu_edit_mensaje);

		btnEnviar = (Button) findViewById(R.id.msu_btn_enviar);
		btnEnviar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				enviarMensaje(null, null);
			}
		});

		boolean cargarMensajes = true;

		try {
			if (savedInstanceState != null) {
				if (savedInstanceState.containsKey(Constantes.PARAM_MENSAJES)) {
					RestRespuestaMensaje rRM = (RestRespuestaMensaje) savedInstanceState
							.getSerializable(Constantes.PARAM_MENSAJES);
					this.pintarMensajesUsuario(rRM, false, false);
					cargarMensajes = false;
				}
				if (savedInstanceState.containsKey(Constantes.PARAM_ID_ULTIMO_MENSAJE)) {
					idUltimoMensajeConsolidado = Integer.valueOf(savedInstanceState
							.getInt(Constantes.PARAM_ID_ULTIMO_MENSAJE));
				}
			}
		} catch (Exception e) {
			MnLog.e(this, "Error al recuperar la instancia con los mensajes: " + e.getMessage(), e);
			cargarMensajes = true;
		}



		if (cargarMensajes) {
			// si los cargamos de BBDD
			mS = new MensajesService(DIALOGO_CARGANDO_CONVERSACIONES, usuarioMSG, app.getUsuarioApp(),
					Constantes.TIPO_ACCION_MENSAJES_OBTENER, null, null, null, null, obtenerIdsPendientesProcesar(),
					null, false, true, this);
			mS.execute("");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			if (app.getNuevosMensajes().size() > 0) {
				RestRespuestaMensaje rRM = null;
				List<RestMensaje> nuevosMensajesUsuario = new ArrayList<RestMensaje>();
				for (RestMensaje nuevoMensaje : app.getNuevosMensajes()) {
					if (nuevoMensaje.getUsuarioEnvia().equals(usuarioMSG)) {
						nuevosMensajesUsuario.add(nuevoMensaje);
					}
				}
				if (nuevosMensajesUsuario.size() > 0) {
					app.eliminarNuevosMensajes(usuarioMSG);

					rRM = new RestRespuestaMensaje();
					rRM.setCodigoRespuesta(Constantes.RESPUESTA_OK_COD);
					rRM.setMsgRespuesta(Constantes.RESPUESTA_OK_MSG);
					rRM.setMensajes(nuevosMensajesUsuario);
					pintarMensajesUsuario(rRM, false, false);

					try {
						String ns = Context.NOTIFICATION_SERVICE;
						NotificationManager nMgr = (NotificationManager) getSystemService(ns);
						// nMgr.cancel(Constantes.NOTIFICACION_ID);
						nMgr.cancel(Constantes.NOTIFICACION_MENSAJE_ID);
					} catch (Exception e) {
						// nada
					}
				}
			}

		} catch (Exception e) {
			MnLog.e(this, "Error al verificar si hay nuevos mensajes: " + e.getMessage(), e);
		}

		registerReceiver(receiver, new IntentFilter(MensajesUsuarioActivity.ACTION_STRING));

		// if (lanzarAutoUpdate) {
		levantarAutoUpdate(false);
		// }
	}

	private void levantarAutoUpdate(boolean ya) {
		MnLog.d(this, "[" + this + "] deBBDD5: " + ya);
		if (ya) {
			try {
				if (autoUpdate != null) {
					autoUpdate.cancel();
					autoUpdate = null;
				}
			} catch (Exception e) {
				MnLog.e(this, "Error al parar el proceso de actualizacion periodica: " + e.getMessage(), e);
			}
		}

		try {
			if (autoUpdate == null) {
				int delay = Constantes.DELAY_AUTOUPDATE_MENSAJES;
				try {
					String confDelay = app.obtenerConfiguracion(ClaveConfiguracion.MENSAJES_RESUMEN_UPDATE_DELAY);
					if (confDelay != null) {
						delay = Integer.valueOf(confDelay);
					}
				} catch (Exception e) {
					MnLog.e(this, "Error al obtener el delay de actualizacion: " + e.getMessage(), e);
				}

				int frecuencia = Constantes.TIEMPO_AUTOUPDATE_MENSAJES;
				try {
					String confFrecuencia = app
							.obtenerConfiguracion(ClaveConfiguracion.MENSAJES_RESUMEN_UPDATE_FRECUENCIA);
					if (confFrecuencia != null) {
						frecuencia = Integer.valueOf(confFrecuencia);
					}
				} catch (Exception e) {
					MnLog.e(this, "Error al obtener la frecuencia de actualizacion: " + e.getMessage(), e);
				}

				autoUpdate = new Timer();
				autoUpdate.schedule(new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							public void run() {
								// Toast.makeText(MensajesUsuarioActivity.this, "Timer!!", Toast.LENGTH_SHORT).show();
								revisarNuevosMensajes();
							}
						});
					}
				}, ya ? 0 : delay, frecuencia);
			}
		} catch (Exception e) {
			MnLog.e(this, "Error al levantar el proceso de actualizacion periodica: " + e.getMessage(), e);
		}
	}


	public void mensajesEliminados(RestRespuestaMensaje rRM, List<Integer> idsMensajesBorrados) {
		if (rRM != null) {
			if (rRM.getCodigoRespuesta() == Constantes.RESPUESTA_OK_COD) {
				if (idsMensajesBorrados != null && idsMensajesBorrados.size() > 0) {
					for (Integer idMensajeBorrado : idsMensajesBorrados) {
						RestMensaje mensajeBorrado = null;
						for (RestMensaje mensaje : ultimosMensajes) {
							if (mensaje.getIdMensaje() == idMensajeBorrado.intValue()) {
								mensajeBorrado = mensaje;
								break;
							}
						}
						if (mensajeBorrado != null) {
							ultimosMensajes.remove(mensajeBorrado);
							mensajesAdapter.notifyDataSetChanged();
						}
					}
				}
				pintarUltimosMensajesInterno();
			} else if (Constantes.RESPUESTA_KO_SIN_ACCESO_INTERNET_COD == rRM.getCodigoRespuesta()) {
				Toast.makeText(this, R.string.msg_sin_acceso_internet, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.msg_problema_general, Toast.LENGTH_SHORT).show();
			}
		}

		cierraDialogoCargando();
	}

	public void imagenGuardada(RestRespuestaMensaje rRM) {
		if (rRM != null) {
			if (rRM.getCodigoRespuesta() == Constantes.RESPUESTA_OK_COD) {
				Toast.makeText(this, R.string.imagen_guardada, Toast.LENGTH_SHORT).show();
			} else if (Constantes.RESPUESTA_KO_GALERIA_LIMITE_SUPERADO_COD == rRM.getCodigoRespuesta()) {
				Toast.makeText(this, R.string.debes_eliminar_imagen, Toast.LENGTH_SHORT).show();
			} else if (Constantes.RESPUESTA_KO_SIN_ACCESO_INTERNET_COD == rRM.getCodigoRespuesta()) {
				Toast.makeText(this, R.string.msg_sin_acceso_internet, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.msg_problema_general, Toast.LENGTH_SHORT).show();
			}
		}

		cierraDialogoCargando();
	}

	protected void revisarNuevosMensajes() {
		if (mS != null) {
			try {
				mS.cancel(true);
			} catch (Exception e) {
				MnLog.e(this, "Error al parar el servicio de busqueda de mensajes");
			}
		}
		if (app.getUsuarioApp() != null) {
			mS = new MensajesService(null, usuarioMSG, app.getUsuarioApp(), Constantes.TIPO_ACCION_MENSAJES_OBTENER,
					null, null, null, idUltimoMensajeConsolidado, obtenerIdsPendientesProcesar(), null, false, false,
					this);
			mS.execute("");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		MnLog.i(this, "MensajesResumenActivity: guardamos la instancia");

		RestRespuestaMensaje rRM = new RestRespuestaMensaje();
		rRM.setCodigoRespuesta(Constantes.RESPUESTA_OK_COD);
		rRM.setMsgRespuesta(Constantes.RESPUESTA_OK_MSG);
		rRM.setMensajes(this.ultimosMensajes);
		List<RestUsuario> usuarios = new ArrayList<RestUsuario>();
		usuarios.add(this.usuario);
		rRM.setUsuarios(usuarios);

		outState.putSerializable(Constantes.PARAM_MENSAJES, rRM);

		if (idUltimoMensajeConsolidado != null) {
			outState.putInt(Constantes.PARAM_ID_ULTIMO_MENSAJE, idUltimoMensajeConsolidado);
		}

		super.onSaveInstanceState(outState);
	}

	protected void recibirMensaje(final RestMensaje nuevoMensaje) {

		// TODO controlar la fecha de los diferentes mensajes para no meter el nuevo al final directamente (puede haber
		// llegado con retraso
		boolean anadir = true;
		for (RestMensaje mensaje : ultimosMensajes) {
			if (mensaje.getIdMensaje() == nuevoMensaje.getIdMensaje()) {
				anadir = false;
				break;
			}
		}
		if (anadir) {
			for (Integer idsRecibidoAnterior : idsUltimosMensajesRecibidos) {
				if (idsRecibidoAnterior.intValue() == nuevoMensaje.getIdMensaje()) {
					anadir = false;
					break;
				}
			}
		}

		if (anadir) {
			idsUltimosMensajesRecibidos.add(nuevoMensaje.getIdMensaje());

			try {
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(Constantes.DURACION_VIBRACION_NUEVO_MENSAJE);
			} catch (Exception e) {
				MnLog.e(this, "Error al vibrar: " + e.getMessage(), e);
			}

			// anadir y notificar
			nuevoMSG = true;
			ultimosMensajes.add(nuevoMensaje);
			mensajesAdapter.notifyDataSetChanged();
			// lvwMensajes.scrollTo(0, 0);

			try {
				ModeloWF modelo = new ModeloWF(this);
				modelo.abrir();

				List<RestMensaje> mensajes = new ArrayList<RestMensaje>();
				mensajes.add(nuevoMensaje);

				modelo.guardarMensajes(mensajes, usuario);

				modelo.cerrar();

			} catch (Exception e) {
				MnLog.e(this, "Error al tratar con la BD: " + e.getMessage(), e);
			}
		}
	}

	protected void enviarMensaje(Uri uri, Integer rotacion) {
		
		String mensaje = editMensaje.getText().toString();
		if (mensaje.trim().length() != 0 || (uri != null && uri.getPath().length() > 0)) {

		

			final RestMensaje restMensaje = new RestMensaje();
			restMensaje.setUsuarioEnvia(app.getUsuarioApp().getLogin());
			restMensaje.setUsuarioRecibe(usuarioMSG);
			restMensaje.setEstado(Constantes.ESTADO_MENSAJE_PENDIENTE);
			restMensaje.setTexto(mensaje);
			restMensaje.setFecha(System.currentTimeMillis());


			editMensaje.setText("");

			Long idPeticion = Long.valueOf(System.currentTimeMillis());
			// this.loadings.put(idPeticion, pgbEnviando);
			// this.loadingsImg.put(idPeticion, imgCheck);
			// this.loadingsImgChat.put(idPeticion, imgMsg);
			// this.loadingsTxtChat.put(idPeticion, txtMensaje);
			// this.loadingsLlyChat.put(idPeticion, llyContenido);
			// this.loadingsMSGView.put(idPeticion, mensajeView);

			this.mensajesPendientes.put(idPeticion, restMensaje);
			nuevoMSG = true;
			this.ultimosMensajes.add(restMensaje);
			mensajesAdapter.notifyDataSetChanged();

			MensajesService mSEnvio = new MensajesService(null, usuarioMSG, app.getUsuarioApp(),
					Constantes.TIPO_ACCION_MENSAJES_ENVIAR, mensaje.trim(), rotacion, uri, null, null, idPeticion,
					false, false, MensajesUsuarioActivity.this);
			mSEnvio.execute("");
		} else { // le llamamos tonto?
		}
	}

	public void pintarMensajesUsuario(RestRespuestaMensaje rRM, boolean vibrar, boolean deBBDD) {
		MnLog.d(this, "[" + this + "] deBBDD4: " + deBBDD);

		boolean nosVamos = false;
		boolean pintar = false;
		boolean nuevoRecibido = false;

		if (rRM != null) {
			if (rRM.getCodigoRespuesta() == Constantes.RESPUESTA_OK_COD) {
				if (ultimosMensajes == null) {
					ultimosMensajes = new ArrayList<RestMensaje>();
				}
				int totalAnterior = ultimosMensajes.size();

				if (rRM.getMensajes() != null && rRM.getMensajes().size() > 0) {
					for (RestMensaje nuevoMensaje : rRM.getMensajes()) {
						boolean anadirMSG = true;
						for (RestMensaje mensaje : ultimosMensajes) {
							if (mensaje.getIdMensaje() == nuevoMensaje.getIdMensaje()) {
								anadirMSG = false;
								break;
							}
						}
						if (anadirMSG) {
							if (!nuevoMensaje.getUsuarioEnvia().equals(app.getUsuarioApp().getLogin())) {
								nuevoRecibido = true;
								for (Integer idsRecibidoAnterior : idsUltimosMensajesRecibidos) {
									if (idsRecibidoAnterior.intValue() == nuevoMensaje.getIdMensaje()) {
										nuevoRecibido = false;
										break;
									}
								}
							}
							ultimosMensajes.add(nuevoMensaje);
						}
					}
					// si hay nuevos hay que ordenar por fecha descendentemente
					if (totalAnterior < ultimosMensajes.size()) {
						pintar = true;
						// Ordenamos los resultados
						ComparadorMensaje comparadorMSG = new ComparadorMensaje();
						comparadorMSG.setClaveComparadora(ComparadorMensaje.CLAVE_FECHA);
						comparadorMSG.setHashComparado(ComparadorMensaje.CLAVE_FECHA);
						Collections.sort(ultimosMensajes, comparadorMSG);
						// Collections.reverse(ultimosMensajes);

						// Nos quedamos con los ultimos X mensajes
						while (ultimosMensajes.size() > Constantes.NUMERO_MAXIMO_MENSAJES_CHAT) {
							ultimosMensajes.remove(0);
						}
					}

					if (rRM.getUsuarios() != null && rRM.getUsuarios().size() > 0) {
						for (RestUsuario nuevoUsuario : rRM.getUsuarios()) {
							if (nuevoUsuario.getLogin().equals(usuarioMSG)) {
								usuario = nuevoUsuario;
								break;
							}
						}
					}
				}

				if (rRM.getIdsMensajes() != null && rRM.getIdsMensajes().size() > 0) {
					for (Integer idProcesado : rRM.getIdsMensajes()) {
						for (RestMensaje mensaje : ultimosMensajes) {
							if (mensaje.getIdMensaje() == idProcesado.intValue()) {
								mensaje.setEstado(Constantes.ESTADO_MENSAJE_PROCESADO);
								pintar = true;
								break;
							}
						}
					}
				}

				// ultimosMensajes = rRM.getMensajes();
				// usuarios = rRM.getUsuarios();

			} else if (Constantes.RESPUESTA_KO_SIN_ACCESO_INTERNET_COD == rRM.getCodigoRespuesta()) {
				Toast.makeText(this, R.string.msg_sin_acceso_internet, Toast.LENGTH_SHORT).show();
				nosVamos = true;
			} else {
				Toast.makeText(this, R.string.msg_problema_cargar_mensajes, Toast.LENGTH_SHORT).show();
				nosVamos = true;
			}
		}
		if (ultimosMensajes == null) {
			Toast.makeText(this, R.string.msg_problema_cargar_mensajes, Toast.LENGTH_SHORT).show();
			nosVamos = true;
		}

		if (nosVamos) {
			if (ultimosMensajes != null && ultimosMensajes.size() > 0) {
				nosVamos = false;
			}
		}

		if (!nosVamos) {
			if (pintar) {
				pintarUltimosMensajesInterno();

				if (vibrar && nuevoRecibido) {
					try {
						Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						vibrator.vibrate(Constantes.DURACION_VIBRACION_NUEVO_MENSAJE);
					} catch (Exception e) {
						MnLog.e(this, "Error al vibrar: " + e.getMessage(), e);
					}
				}

				if (ultimosMensajes.size() > 0) {
					idUltimoMensajeConsolidado = ultimosMensajes.get(ultimosMensajes.size() - 1).getIdMensaje();
				}
			}

			cierraDialogoCargando();

			levantarAutoUpdate(deBBDD);
		} else {
			nosVamos(true);
		}
	}

	private void pintarUltimosMensajesInterno() {
		mensajesAdapter.notifyDataSetChanged();

	

		// Pintamos si el usuario esta bloqueado o no
		pintarBloqueoUsuario();
	}

	public void resultadoBloqueo(RestRespuestaUsuario rRU, boolean bloquear) {
		if (rRU != null) {
			if (rRU.getCodigoRespuesta() == Constantes.RESPUESTA_OK_COD) {
				if (bloquear) {
					this.usuario.setBloqueado(Constantes.TIPO_BLOQUEO_USUARIO_MENSAJES);
				} else {
					this.usuario.setBloqueado(null);
				}
				pintarBloqueoUsuario();
			} else if (Constantes.RESPUESTA_KO_SIN_ACCESO_INTERNET_COD == rRU.getCodigoRespuesta()) {
				Toast.makeText(this, R.string.msg_sin_acceso_internet, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.msg_problema_general, Toast.LENGTH_SHORT).show();
			}
		}

		cierraDialogoCargando();
	}

	private void pintarBloqueoUsuario() {
		if (usuario.getBloqueado() != null && usuario.getBloqueado().intValue() > 0) {
			imgBloquear.setImageResource(R.drawable.icono_bloqueado);
			imgBloquear.setContentDescription(getResources().getText(R.string.desbloquear_usuario));
		} else {
			imgBloquear.setImageResource(R.drawable.icono_desbloqueado);
			imgBloquear.setContentDescription(getResources().getText(R.string.bloquear_usuario));
		}
	}

	public void mensajeEnviado(final RestRespuestaMensaje rRM, Long idPeticion) {

		// boolean enviado = false;
		if (rRM != null) {
			if (rRM.getCodigoRespuesta() == Constantes.RESPUESTA_OK_COD) {
				RestMensaje msgpdte = this.mensajesPendientes.remove(idPeticion);
				if (msgpdte != null) {
					ultimosMensajes.remove(msgpdte);
				}
				ultimosMensajes.add(rRM.getMensaje());
				nuevoMSG = true;
				mensajesAdapter.notifyDataSetChanged();
				// enviado = true;
			} else if (Constantes.RESPUESTA_KO_SIN_ACCESO_INTERNET_COD == rRM.getCodigoRespuesta()) {
				Toast.makeText(this, R.string.msg_sin_acceso_internet, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, R.string.msg_problema_enviar_mensaje, Toast.LENGTH_SHORT).show();
			}
		}

	

	}

	private void cierraDialogoCargando() {
		try {
			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}
		} catch (Exception e) {
			MnLog.e(this, "Error: " + e.getMessage(), e);
		}
	}

	@Override
	public void onBackPressed() {
		nosVamos(false);
	}

	public void nosVamos(boolean sinDatos) {
		Intent intent = new Intent();
		if (ultimosMensajes != null && ultimosMensajes.size() > 0) {
			try {
				Bundle b = new Bundle();
				RestMensaje ultimoMensaje = ultimosMensajes.get(ultimosMensajes.size() - 1);
				b.putSerializable(Constantes.PARAM_MENSAJE, ultimoMensaje);
				if (usuario != null) {
					b.putSerializable(Constantes.PARAM_USUARIO, usuario);
				}
				intent.putExtras(b);
			} catch (Exception e) {
				MnLog.e(this, "Error al notificar el ultimo mensaje de la conversacion");
			}
		}

		setResult(Constantes.RESPUESTA_OK_COD, intent);

		finish();
	}

	@Override
	protected void onDestroy() {
		MnLog.i(this, "VerUsuarioActivity - Destroy");

		if (mS != null) {
			mS.cancel(true);
			mS = null;
		}
		cierraDialogoCargando();
		super.onDestroy();
	}

	@Override
	protected void onPause() {

		try {
			if (autoUpdate != null) {
				autoUpdate.cancel();
				autoUpdate = null;
			}
		} catch (Exception e) {
			MnLog.e(this, "Error al parar el proceso de actualizacion periodica: " + e.getMessage(), e);
		}

		unregisterReceiver(receiver);

		overridePendingTransition(0, 0);

		super.onPause();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOGO_CARGANDO_CONVERSACIONES:
			MnLog.i(this, "Mostrando dialogo cargando mensajes " + this);
			if (pDialog != null && pDialog.isShowing()) {
				return null;
			}
			pDialog = new ProgressDialog(this);
			pDialog.setMessage(getResources().getString(R.string.cargando_mensajes));
			pDialog.setIndeterminate(true);
			pDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_bar_states));
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					MensajesUsuarioActivity.this.nosVamos(true);
				}
			});
			return pDialog;

		case DIALOGO_ELIMINANDO_MENSAJE:
			MnLog.i(this, "Mostrando dialogo eliminando_mensaje " + this);
			if (pDialog != null && pDialog.isShowing()) {
				return null;
			}
			pDialog = new ProgressDialog(this);
			pDialog.setMessage(getResources().getString(R.string.eliminando_mensaje));
			pDialog.setIndeterminate(true);
			pDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_bar_states));
			pDialog.setCancelable(true);
			return pDialog;

		case DIALOGO_GUARDANDO_GALERIA:
			MnLog.i(this, "Mostrando dialogo guardando galeria " + this);
			if (pDialog != null && pDialog.isShowing()) {
				return null;
			}
			pDialog = new ProgressDialog(this);
			pDialog.setMessage(getResources().getString(R.string.guardardo));
			pDialog.setIndeterminate(true);
			pDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_bar_states));
			pDialog.setCancelable(true);
			return pDialog;

		case DIALOGO_BLOQUEANDO_USUARIO:
			MnLog.i(this, "Mostrando dialogo bloqueando usuario " + this);
			if (pDialog != null && pDialog.isShowing()) {
				return null;
			}
			pDialog = new ProgressDialog(this);
			pDialog.setMessage(getResources().getString(R.string.bloqueando_usuario));
			pDialog.setIndeterminate(true);
			pDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_bar_states));
			pDialog.setCancelable(true);
			return pDialog;

		case DIALOGO_DESBLOQUEANDO_USUARIO:
			MnLog.i(this, "Mostrando dialogo desbloqueando usuario " + this);
			if (pDialog != null && pDialog.isShowing()) {
				return null;
			}
			pDialog = new ProgressDialog(this);
			pDialog.setMessage(getResources().getString(R.string.desbloqueando_usuario));
			pDialog.setIndeterminate(true);
			pDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_bar_states));
			pDialog.setCancelable(true);
			return pDialog;

		}
		return null;
	}

	private void cargarDesdeGaleria() {
		try {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			photoPickerIntent.setType("image/*");
			// photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
			// photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
		} catch (Exception e) {
			MnLog.e(this, "Error: " + e.getMessage(), e);
			Toast.makeText(this, R.string.msg_problema_cargar_imagen, Toast.LENGTH_SHORT).show();

		}
	}

	private void cargarDesdeCamara() {
		try {
			Intent photoPickerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// photoPickerIntent.setType("image/*");
			photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri(true));
			photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			// startActivityForResult(photoPickerIntent, REQ_CODE_TAKE_IMAGE);
			startActivityForResult(photoPickerIntent, REQ_CODE_TAKE_IMAGE);
		} catch (Exception e) {
			MnLog.e(this, "Error: " + e.getMessage(), e);
			Toast.makeText(this, R.string.msg_problema_cargar_imagen, Toast.LENGTH_SHORT).show();

		}
	}

	private Uri getTempUri(boolean crearPublica) {
		return Uri.fromFile(getTempFile(crearPublica));
	}

	@SuppressLint("WorldWriteableFiles")
	private File getTempFile(boolean crearPublica) {
		if (isSDCARDMounted()) {
			File d = new File(Environment.getExternalStorageDirectory(), "WhosFuckin/tmp");
			if (!d.exists()) {
				d.mkdirs();
			}

			/*
			 * File path=new File(getFilesDir(),"WhosFuckin"); path.mkdirs();
			 */

			File f = new File(d, TEMP_PHOTO_FILE);
			try {
				f.createNewFile();
			} catch (IOException e) {

			}
			return f;
		} else {
			File f = new File(getFilesDir(), TEMP_PHOTO_FILE);
			if (crearPublica) {
				try {
					if (f.exists()) {
						f.delete();
					}
					FileOutputStream fos = openFileOutput(TEMP_PHOTO_FILE, Context.MODE_WORLD_WRITEABLE);
					fos.close();
					f = new File(getFilesDir(), TEMP_PHOTO_FILE);
				} catch (IOException e) {

				}
			} else {
				try {
					f.createNewFile();
				} catch (IOException e) {

				}
			}
			return f;
		}
	}

	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		try {
			if (resultCode != RESULT_OK)
				return;

			switch (requestCode) {

			case REQ_CODE_PICK_IMAGE:
				if (resultCode == RESULT_OK) {
					if (data != null) {

						Uri selectedImage = data.getData();

						boolean picasaImage = (selectedImage.toString().startsWith(
								"content://com.google.android.gallery3d") || selectedImage.toString().startsWith(
								"content://com.sec.android.gallery3d.provider/picasa"));

						if (picasaImage) {
							Toast.makeText(this, R.string.msg_picasa_no_soportado, Toast.LENGTH_LONG).show();
							break;
						}

						String[] filePathColumn = { MediaStore.Images.Media.DATA };

						Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
						cursor.moveToFirst();

						int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
						String picturePath = cursor.getString(columnIndex);
						cursor.close();

						int rotateImage = ImageUtil.getCameraPhotoOrientation(getContentResolver(), selectedImage,
								picturePath);

						try {
							ImageUtil
									.resizeImage(picturePath, getTempUri(false).getPath(), imagenAnchura, imagenAltura);
							picturePath = getTempUri(false).getPath();
						} catch (Exception e) {
							MnLog.e(this,
									"Error al tratar la redimensionar imagen, se envia completa: " + e.getMessage(), e);

						}

						enviarMensaje(Uri.parse(picturePath), rotateImage);

					}
				}
				break;
			case REQ_CODE_TAKE_IMAGE:
				if (resultCode == RESULT_OK) {
					// if (data != null) {
					/*
					 * Bitmap bitmap = BitmapFactory.decodeFile(getTempUri().getPath()); System.out.println("path2 " +
					 * getTempUri().getPath());
					 * 
					 * ByteArrayOutputStream stream = new ByteArrayOutputStream();
					 * bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); byte[] byteArray = stream.toByteArray();
					 */
					int rotateImage = ImageUtil.getCameraPhotoOrientation(getContentResolver(), getTempUri(false),
							getTempUri(false).getPath());

					try {
						ImageUtil.resizeImage(getTempUri(false).getPath(), getTempUri(false).getPath(), imagenAnchura,
								imagenAltura);
					} catch (Exception e) {
						MnLog.e(this, "Error al tratar la redimensionar imagen, se envia completa: " + e.getMessage(),
								e);

					}

					enviarMensaje(getTempUri(false), rotateImage);

					// }
				}
				break;
			}
		} catch (Exception e) {
			MnLog.e(this, "Error al tratar la respuesta: " + e.getMessage(), e);

		}
	}

	private void verUsuario() {
		Intent i = new Intent(MensajesUsuarioActivity.this, VerUsuarioActivity.class);
		Bundle b = new Bundle();
		b.putString(Constantes.PARAM_USUARIO_ID, usuarioMSG);
		i.putExtras(b);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		MensajesUsuarioActivity.this.startActivity(i);
	}

	private List<Integer> obtenerIdsPendientesProcesar() {
		List<Integer> idsPendientes = new ArrayList<Integer>();
		if (ultimosMensajes != null && ultimosMensajes.size() > 0) {
			for (RestMensaje mensaje : ultimosMensajes) {
				if (mensaje.getEstado() == null) {
					mensaje.setEstado(Constantes.ESTADO_MENSAJE_PENDIENTE);
				}
				if (mensaje.getUsuarioEnvia().equals(app.getUsuarioApp().getLogin())
						&& mensaje.getEstado().equals(Constantes.ESTADO_MENSAJE_PENDIENTE)) {
					idsPendientes.add(mensaje.getIdMensaje());
				}
			}
		}

		return idsPendientes;
	}

	private class ManejadorLongClick implements View.OnLongClickListener {

		private RestMensaje mensaje;
		// private final View mensajeView;
		private final boolean copiar;

		private static final int OPCION_COPIAR = 1;
		private static final int OPCION_GUARDAR_GALERIA = 2;
		private static final int OPCION_ELIMINAR = 3;

		public ManejadorLongClick(RestMensaje mensaje, boolean copiar) {
			this.mensaje = mensaje;
			// this.mensajeView = mensajeView;
			this.copiar = copiar;
		}

		@Override
		public boolean onLongClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MensajesUsuarioActivity.this);

			CharSequence[] opciones;

			if (copiar) {
				opciones = new CharSequence[] { getString(R.string.copiar_mensaje),
						getString(R.string.eliminar_mensaje) };
			} else {
				// Estamos en una imagen
				if (mensaje.getUsuarioEnvia().equals(app.getUsuarioApp().getLogin())) {
					opciones = new CharSequence[] { getString(R.string.guardar_en_galeria),
							getString(R.string.eliminar_mensaje) };
				} else {
					opciones = new CharSequence[] { getString(R.string.eliminar_mensaje) };
				}
			}

			builder.setItems(opciones, new DialogInterface.OnClickListener() {

				@SuppressWarnings("deprecation")
				@SuppressLint("NewApi")
				public void onClick(DialogInterface dialog, int item) {

					int opcion_elegida = -1;

					if (copiar) {
						if (item == 0) {
							opcion_elegida = OPCION_COPIAR;
						} else if (item == 1) {
							opcion_elegida = OPCION_ELIMINAR;
						}
					} else {
						if (mensaje.getUsuarioEnvia().equals(app.getUsuarioApp().getLogin())) {
							if (item == 0) {
								opcion_elegida = OPCION_GUARDAR_GALERIA;
							} else if (item == 1) {
								opcion_elegida = OPCION_ELIMINAR;
							}
						} else {
							if (item == 0) {
								opcion_elegida = OPCION_ELIMINAR;
							}
						}
					}

					List<Integer> idsMensajes;

					switch (opcion_elegida) {
					case OPCION_ELIMINAR:
						// Borramos el mensaje
						// mensajeView.setVisibility(View.GONE);
						// Toast.makeText(getApplicationContext(), "Borrar", Toast.LENGTH_SHORT).show();
						idsMensajes = new ArrayList<Integer>();
						idsMensajes.add(mensaje.getIdMensaje());

						mS = new MensajesService(DIALOGO_ELIMINANDO_MENSAJE, null, app.getUsuarioApp(),
								Constantes.TIPO_ACCION_MENSAJES_BORRAR, null, null, null, null, idsMensajes, null,
								false, false, MensajesUsuarioActivity.this);
						mS.execute("");

						break;
					case OPCION_COPIAR:
						// Copiamos el mensaje
						int sdk = android.os.Build.VERSION.SDK_INT;
						if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(mensaje.getTexto());
						} else {
							// android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
							// getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("WhosFuckin",
									mensaje.getTexto());
							clipboard.setPrimaryClip(clip);
						}

						break;
					case OPCION_GUARDAR_GALERIA:
						mS = new MensajesService(DIALOGO_GUARDANDO_GALERIA, null, app.getUsuarioApp(),
								Constantes.TIPO_ACCION_MENSAJES_GUARDAR_GALERIA, mensaje.getImagen(), null, null,
								mensaje.getIdMensaje(), null, null, false, false, MensajesUsuarioActivity.this);
						mS.execute("");

						break;
					default:
						break;
					}
					dialog.dismiss();
				}
			});
			builder.show();

			/*
			 * if (copiar) {
			 * 
			 * builder.setItems(new CharSequence[] { getString(R.string.eliminar_mensaje),
			 * getString(R.string.copiar_mensaje) }, new DialogInterface.OnClickListener() {
			 * 
			 * @SuppressWarnings("deprecation")
			 * 
			 * @SuppressLint("NewApi") public void onClick(DialogInterface dialog, int item) { //
			 * Toast.makeText(getApplicationContext(), sexoCharSeq[item], // Toast.LENGTH_SHORT).show(); //
			 * txvSexo.setText(sexoCharSeq[item]); switch (item) { case 0: // Borramos el mensaje //
			 * mensajeView.setVisibility(View.GONE); // Toast.makeText(getApplicationContext(), "Borrar",
			 * Toast.LENGTH_SHORT).show(); List<Integer> idsMensajes = new ArrayList<Integer>();
			 * idsMensajes.add(mensaje.getIdMensaje());
			 * 
			 * mS = new MensajesService(DIALOGO_ELIMINANDO_MENSAJE, null, app.getUsuarioApp(),
			 * Constantes.TIPO_ACCION_MENSAJES_BORRAR, null, null, null, null, idsMensajes, null, false, false,
			 * MensajesUsuarioActivity.this); mS.execute("");
			 * 
			 * break; case 1: // Copiamos el mensaje int sdk = android.os.Build.VERSION.SDK_INT; if (sdk <
			 * android.os.Build.VERSION_CODES.HONEYCOMB) { android.text.ClipboardManager clipboard =
			 * (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			 * clipboard.setText(mensaje.getTexto()); } else { // android.content.ClipboardManager clipboard =
			 * (android.content.ClipboardManager) // getSystemService(Context.CLIPBOARD_SERVICE);
			 * android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
			 * getSystemService(Context.CLIPBOARD_SERVICE); android.content.ClipData clip =
			 * android.content.ClipData.newPlainText("WhosFuckin", mensaje.getTexto()); clipboard.setPrimaryClip(clip);
			 * }
			 * 
			 * break; default: break; } dialog.dismiss(); } }); builder.show(); } else { builder.setItems(new
			 * CharSequence[] { getString(R.string.eliminar_mensaje) }, new DialogInterface.OnClickListener() {
			 * 
			 * @SuppressLint("NewApi") public void onClick(DialogInterface dialog, int item) { //
			 * Toast.makeText(getApplicationContext(), sexoCharSeq[item], // Toast.LENGTH_SHORT).show(); //
			 * txvSexo.setText(sexoCharSeq[item]); switch (item) { case 0: // Borramos el mensaje //
			 * mensajeView.setVisibility(View.GONE); // Toast.makeText(getApplicationContext(), "Borrar",
			 * Toast.LENGTH_SHORT).show(); List<Integer> idsMensajes = new ArrayList<Integer>();
			 * idsMensajes.add(mensaje.getIdMensaje());
			 * 
			 * mS = new MensajesService(DIALOGO_ELIMINANDO_MENSAJE, null, app.getUsuarioApp(),
			 * Constantes.TIPO_ACCION_MENSAJES_BORRAR, null, null, null, null, idsMensajes, null, false, false,
			 * MensajesUsuarioActivity.this); mS.execute("");
			 * 
			 * break; default: break; } dialog.dismiss(); } }); builder.show(); }
			 */

			return true;
		}
	}

	private class MensajesArrayAdapter extends ArrayAdapter<RestMensaje> {

		private final Context context;
		private final int layoutResourceId;
		private final List<RestMensaje> data;

		public MensajesArrayAdapter(Context context, int layoutResourceId, List<RestMensaje> data) {
			super(context, layoutResourceId, data);
			this.context = context;
			this.layoutResourceId = layoutResourceId;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			MensajeHolder holder;

			if (row == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new MensajeHolder();
				holder.llyCompleta = (LinearLayout) row.findViewById(R.id.mll_lly_completa);
				holder.llyContenido = (LinearLayout) row.findViewById(R.id.mll_lly_contenido);
				holder.txtMensaje = (TextView) row.findViewById(R.id.mll_txt_mensaje);
				holder.imgMsg = (ImageView) row.findViewById(R.id.mll_img_msg);
				holder.txtFecha = (TextView) row.findViewById(R.id.mll_txt_fecha);
				holder.imgCheck = (ImageView) row.findViewById(R.id.mll_img_check);
				holder.imgCheckDoble = (ImageView) row.findViewById(R.id.mll_img_check_doble);
				holder.pgbLoading = (ProgressBar) row.findViewById(R.id.mll_pgb_progress_bar);

				row.setTag(holder);
			} else {
				holder = (MensajeHolder) row.getTag();
			}

			final RestMensaje mensaje = data.get(position);

			// final View mensajeView = inflater.inflate(R.layout.mensaje_list_layout, null);

			boolean yo = app.getUsuarioApp().getLogin().equals(mensaje.getUsuarioEnvia());

			MnLog.d(this, "mensaje: " + mensaje);
			MnLog.d(this, "mensaje.fecha: " + mensaje.getFecha());
			MnLog.d(this, "holder: " + holder);
			MnLog.d(this, "holder.txtFecha: " + holder.txtFecha);

			holder.txtFecha.setText(Utils.formateaFecha(mensaje.getFecha(), context, true));
			if (mensaje.getImagen() != null && mensaje.getImagen().length() > 0) {
				holder.imgMsg.setVisibility(View.VISIBLE);
				MnLog.d(this, "cache - C - (" + mensaje.getImagen() + ") w:" + mensaje.imgWidth + ", h:"
						+ mensaje.imgHeight);
				// int himg = holder.imgMsg.getLayoutParams().height;
				// int wimg = holder.imgMsg.getLayoutParams().width;
				if (mensaje.imgHeight > 0 && mensaje.imgWidth > 0) {
					holder.imgMsg.getLayoutParams().height = mensaje.imgHeight;
					holder.imgMsg.getLayoutParams().width = mensaje.imgWidth;
				} else {
					holder.imgMsg.getLayoutParams().height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
					holder.imgMsg.getLayoutParams().width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
				}
				// WFLog.d(this, "cache - C - (" + mensaje.getImagen() + ") w:" + mensaje.imgWidth + "(" + wimg + " -> "
				// + holder.imgMsg.getLayoutParams().width + "), h:" + mensaje.imgHeight + "(" + himg + " -> "
				// + holder.imgMsg.getLayoutParams().height + ")");
				holder.txtMensaje.setVisibility(View.GONE);
				// ImageCacheUtil.getCache().download(mensaje.getIdMensaje(), mensaje.getImagen(), imgMsg,
				// app.getUsuarioApp(), R.drawable.img_usuario_camara, app);

				ImageLoader.getInstance().displayImage(
						ImageCacheUtil.obtenerURL(mensaje.getIdMensaje(), mensaje.getImagen(), app.getUsuarioApp(),
								context), holder.imgMsg, app.dioIMGChat, new ImageLoadingListener() {
							@Override
							public void onLoadingStarted(String imageUri, View view) {
								// WFLog.d(this, "Empezando!! " + imageUri);
								/*
								 * if (data.indexOf(mensaje) == data.size() - 1 && nuevoMSG) { nuevoMSG = false; }
								 */
							}

							@Override
							public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
								MnLog.w(this, "Error!! " + imageUri + " failReason: "
										+ (failReason != null ? failReason.toString() : "nulo"));
								if (data.indexOf(mensaje) == data.size() - 1 && nuevoMSG) {
									nuevoMSG = false;
								}
							}

							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
								int hAnt = mensaje.imgHeight;
								int wAnt = mensaje.imgWidth;

								// int vH = view.getHeight();
								// int vW = view.getWidth();
								if (hAnt <= 0 && wAnt <= 0) {
									mensaje.imgHeight = loadedImage.getHeight();
									mensaje.imgWidth = loadedImage.getWidth();
									// WFLog.d(this, "cache - I - (" + mensaje.getImagen() + " - " + vW + ") w:"
									// + mensaje.imgWidth + " (" + wAnt + "), h:" + mensaje.imgHeight + " ("
									// + hAnt + " - " + vH + ") S " + imageUri);
								} else {

									// WFLog.d(this, "cache - I - (" + mensaje.getImagen() + " - " + vW + ") w:"
									// + mensaje.imgWidth + " (" + wAnt + "), h:" + mensaje.imgHeight + " ("
									// + hAnt + " - " + vH + ") N " + imageUri);
								}

								if (data.indexOf(mensaje) == data.size() - 1 && nuevoMSG) {
									// WFLog.d(this, "Hacemos scroll...");
									// lvwMensajes.smoothScrollToPosition(data.size() - 1);
									lvwMensajes.post(new Runnable() {
										public void run() {
											lvwMensajes.setSelection(lvwMensajes.getCount() - 1);
										}
									});
									nuevoMSG = false;
								} else {
									// WFLog.d(this, "No hacemos scroll: " + data.indexOf(mensaje) + " _ "
									// + (data.size() - 1) + " " + nuevoMSG);
								}

							}

							@Override
							public void onLoadingCancelled(String imageUri, View view) {
								MnLog.d(this, "Cancelado!! " + imageUri);
								if (data.indexOf(mensaje) == data.size() - 1 && nuevoMSG) {
									nuevoMSG = false;
								}
							}
						});

				holder.imgMsg.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent i = new Intent(MensajesUsuarioActivity.this, VerImagenCompletaActivity.class);
						Bundle extras = new Bundle();
						extras.putInt(Constantes.PARAM_ID_IMAGEN_USUARIO, mensaje.getIdMensaje());
						extras.putString(Constantes.PARAM_NOMBRE_IMAGEN_USUARIO, mensaje.getImagen());
						i.putExtras(extras);
						MensajesUsuarioActivity.this.startActivity(i);
					}
				});

				holder.imgMsg.setOnLongClickListener(new ManejadorLongClick(mensaje, false));

				holder.llyContenido.setOnLongClickListener(new ManejadorLongClick(mensaje, false));
			} else {
				holder.imgMsg.setVisibility(View.GONE);
				holder.txtMensaje.setVisibility(View.VISIBLE);
				holder.txtMensaje.setText(mensaje.getTexto());

				holder.txtMensaje.setOnLongClickListener(new ManejadorLongClick(mensaje, true));

				holder.llyContenido.setOnLongClickListener(new ManejadorLongClick(mensaje, true));
				if (data.indexOf(mensaje) == data.size() - 1 && nuevoMSG) {
					nuevoMSG = false;
				}
			}

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			holder.imgCheck.setVisibility(View.GONE);
			holder.imgCheckDoble.setVisibility(View.GONE);
			holder.pgbLoading.setVisibility(View.GONE);
			if (yo) {
				if (mensaje.getEstado().equals(Constantes.ESTADO_MENSAJE_PROCESADO)) {
					holder.imgCheckDoble.setVisibility(View.VISIBLE);
				} else {
					if (mensajesPendientes.containsValue(mensaje)) {
						holder.pgbLoading.setVisibility(View.VISIBLE);
					} else {
						holder.imgCheck.setVisibility(View.VISIBLE);
					}
				}
				params.gravity = Gravity.RIGHT;
				holder.llyContenido.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_msg_mio));
				holder.txtMensaje.setGravity(Gravity.RIGHT);
				holder.llyCompleta.setGravity(Gravity.RIGHT);
			} else {
				params.gravity = Gravity.LEFT;
				holder.llyContenido.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_msg_suyo));
				holder.txtMensaje.setGravity(Gravity.LEFT);
				holder.llyCompleta.setGravity(Gravity.LEFT);

			}
			params.bottomMargin = 8;

			return row;
		}

	}

	private static class MensajeHolder {
		public LinearLayout llyCompleta;
		public LinearLayout llyContenido;
		public TextView txtMensaje;
		public ImageView imgMsg;
		public TextView txtFecha;
		public ImageView imgCheck;
		public ImageView imgCheckDoble;
		public ProgressBar pgbLoading;
	}

}
