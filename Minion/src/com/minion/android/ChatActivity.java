package com.minion.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import com.desasic.whosfuckin.R;
import com.desasic.whosfuckin.MensajesUsuarioActivity.MensajesArrayAdapter;
import com.desasic.whosfuckin.util.Constantes;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChatActivity extends Activity {

	MinionApplication application;
	private boolean nuevoMSG = false;
	private Hashtable<Long, MensajeDto> mensajesPendientes = new Hashtable<Long, MensajeDto>();

	private List<MensajeDto> ultimosMensajes = new ArrayList<MensajeDto>();
	private List<Integer> idsUltimosMensajesRecibidos = new ArrayList<Integer>();

	private TextView loginUsuario;
	private ImageView imgUsuario;
	private ListView lvwMensajes;
	private String usuarioMSG;
	private Button btnEnviar;
	private EditText editMensaje;
	
	private MensajesArrayAdapter mensajesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_chat);

		application = (MinionApplication) getApplication();
		
		
		loginUsuario = (TextView) findViewById(R.id.msu_txt_login);
		imgUsuario = (ImageView) findViewById(R.id.msu_img_usuario);
		
		lvwMensajes = (ListView) findViewById(R.id.msu_lvw_mensajes);
		lvwMensajes.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lvwMensajes.setStackFromBottom(true);
		mensajesAdapter = new MensajesArrayAdapter(this, R.layout.mensaje_list_layout, ultimosMensajes);
		lvwMensajes.setAdapter(mensajesAdapter);
		boolean pauseOnScroll = false; // or true
		boolean pauseOnFling = true; // or false
		
//		PauseOnScrollListener listener = new PauseOnScrollListener(ImageLoader.getInstance(), pauseOnScroll,
//				pauseOnFling);
		
		
		editMensaje = (EditText) findViewById(R.id.msu_edit_mensaje);
		
		
		btnEnviar = (Button) findViewById(R.id.msu_btn_enviar);
		btnEnviar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//enviarMensaje(null, null);
			}
		});
		
	}

	public void pintarMensajesUsuario(MinionMensajeResponse rRM, boolean vibrar) {

		boolean nosVamos = false;
		boolean pintar = false;
		boolean nuevoRecibido = false;

		if (rRM != null) {
			if (rRM.getResponseCode() == Constantes.RESPUESTA_OK_COD) {
				if (ultimosMensajes == null) {
					ultimosMensajes = new ArrayList<MensajeDto>();
				}
				int totalAnterior = ultimosMensajes.size();

				if (rRM.getMensajeDtoList() != null && rRM.getMensajeDtoList().size() > 0) {
					for (MensajeDto nuevoMensaje : rRM.getMensajeDtoList()) {
						boolean anadirMSG = true;
						for (MensajeDto mensaje : ultimosMensajes) {
							if (mensaje.getMenID().intValue() == nuevoMensaje.getMenID().intValue()) {
								anadirMSG = false;
								break;
							}
						}
						if (anadirMSG) {
							if (!nuevoMensaje.getMenUsuEnvia().equals(application.getUsuario())) {
								nuevoRecibido = true;
								for (Integer idsRecibidoAnterior : idsUltimosMensajesRecibidos) {
									if (idsRecibidoAnterior.intValue() == nuevoMensaje.getMenID().intValue()) {
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

				}

				// ultimosMensajes = rRM.getMensajes();
				// usuarios = rRM.getUsuarios();

				// } else if (Constantes.RESPUESTA_KO_SIN_ACCESO_INTERNET_COD == rRM.getCodigoRespuesta()) {
				// Toast.makeText(this, R.string.msg_sin_acceso_internet, Toast.LENGTH_SHORT).show();
				// nosVamos = true;
				// } else {
				// Toast.makeText(this, R.string.msg_problema_cargar_mensajes, Toast.LENGTH_SHORT).show();
				// nosVamos = true;
			}
		}
		// if (ultimosMensajes == null) {
		// Toast.makeText(this, R.string.msg_problema_cargar_mensajes, Toast.LENGTH_SHORT).show();
		// nosVamos = true;
		// }
		//
		// if (nosVamos) {
		// if (ultimosMensajes != null && ultimosMensajes.size() > 0) {
		// nosVamos = false;
		// }
		// }
		//
		// if (!nosVamos) {
		// if (pintar) {
		//
		//
		// if (vibrar && nuevoRecibido) {
		// try {
		// Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// vibrator.vibrate(Constantes.DURACION_VIBRACION_NUEVO_MENSAJE);
		// } catch (Exception e) {
		// MnLog.e(this, "Error al vibrar: " + e.getMessage(), e);
		// }
		// }
		//
		// if (ultimosMensajes.size() > 0) {
		// idUltimoMensajeConsolidado = ultimosMensajes.get(ultimosMensajes.size() - 1).getIdMensaje();
		// }
		// }
		//
		//
		//
		// } else {
		// nosVamos(true);
		// }
	}

	private class MensajesArrayAdapter extends ArrayAdapter<MensajeDto> {

		private final Context context;
		private final int layoutResourceId;
		private final List<MensajeDto> data;

		public MensajesArrayAdapter(Context context, int layoutResourceId, List<MensajeDto> data) {
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

			final MensajeDto mensaje = data.get(position);

			// final View mensajeView = inflater.inflate(R.layout.mensaje_list_layout, null);

			boolean yo = application.getUsuario().equals(mensaje.getMenUsuEnvia());

			MnLog.d(this, "mensaje: " + mensaje);
			MnLog.d(this, "mensaje.fecha: " + mensaje.getMenFecha());
			MnLog.d(this, "holder: " + holder);
			MnLog.d(this, "holder.txtFecha: " + holder.txtFecha);

			holder.txtFecha.setText(Utils.formateaFecha(mensaje.getMenFecha(), context, true));

			holder.imgMsg.setVisibility(View.GONE);
			holder.txtMensaje.setVisibility(View.VISIBLE);
			holder.txtMensaje.setText(mensaje.getMenTexto());

			holder.txtMensaje.setOnLongClickListener(new ManejadorLongClick(mensaje));

			holder.llyContenido.setOnLongClickListener(new ManejadorLongClick(mensaje));
			if (data.indexOf(mensaje) == data.size() - 1 && nuevoMSG) {
				nuevoMSG = false;
			}

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			holder.imgCheck.setVisibility(View.GONE);
			holder.imgCheckDoble.setVisibility(View.GONE);
			holder.pgbLoading.setVisibility(View.GONE);
			if (yo) {
				if (mensaje.getMenEstado().equals(Constantes.ESTADO_PROCESADO)) {
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

	private class ManejadorLongClick implements View.OnLongClickListener {

		private MensajeDto mensaje;

		private static final int OPCION_COPIAR = 1;
		// private static final int OPCION_GUARDAR_GALERIA = 2;
		private static final int OPCION_ELIMINAR = 3;

		public ManejadorLongClick(MensajeDto mensaje) {
			this.mensaje = mensaje;

		}

		@Override
		public boolean onLongClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

			CharSequence[] opciones;

			opciones = new CharSequence[] { getString(R.string.copiar_mensaje), getString(R.string.eliminar_mensaje) };

			builder.setItems(opciones, new DialogInterface.OnClickListener() {

				@SuppressWarnings("deprecation")
				@SuppressLint("NewApi")
				public void onClick(DialogInterface dialog, int item) {

					int opcion_elegida = -1;

					if (item == 0) {
						opcion_elegida = OPCION_COPIAR;
					} else if (item == 1) {
						opcion_elegida = OPCION_ELIMINAR;
					}

					List<Integer> idsMensajes;

					switch (opcion_elegida) {
					case OPCION_ELIMINAR:
						// Borramos el mensaje
						// mensajeView.setVisibility(View.GONE);
						// Toast.makeText(getApplicationContext(), "Borrar", Toast.LENGTH_SHORT).show();
						idsMensajes = new ArrayList<Integer>();
						idsMensajes.add(mensaje.getMenID());

						// TODO: Servicio para eliminar mensajes
						// mS = new MensajesService(DIALOGO_ELIMINANDO_MENSAJE, null, app.getUsuarioApp(),
						// Constantes.TIPO_ACCION_MENSAJES_BORRAR, null, null, null, null, idsMensajes, null,
						// false, false, MensajesUsuarioActivity.this);
						// mS.execute("");

						break;
					case OPCION_COPIAR:
						// Copiamos el mensaje
						int sdk = android.os.Build.VERSION.SDK_INT;
						if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(mensaje.getMenTexto());
						} else {
							// android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
							// getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("Minion",
									mensaje.getMenTexto());
							clipboard.setPrimaryClip(clip);
						}

						break;

					default:
						break;
					}
					dialog.dismiss();
				}
			});
			builder.show();

			return true;
		}
	}
}
