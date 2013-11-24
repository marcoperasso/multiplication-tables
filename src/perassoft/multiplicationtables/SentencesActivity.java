package perassoft.multiplicationtables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SentencesActivity extends Activity implements
		OnEditorActionListener, OnCheckedChangeListener {

	private static final int menuDeleteSentence = 0;
	private Sentences sentences;
	private int mActiveSentence;
	private EditText sentence;
	private ListView listView;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sentences);

		sentences = Sentences.getSentences(this);
		listView = (ListView) findViewById(R.id.listViewSentences);
		registerForContextMenu(listView);
		sentence = (EditText) findViewById(R.id.editTextSentence);
		sentence.setOnEditorActionListener(this);
		sentence.setImeOptions(EditorInfo.IME_ACTION_DONE);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, sentences);

		listView.setAdapter(adapter);
		CheckBox cbMine = (CheckBox) findViewById(R.id.cbMine);
		cbMine.setChecked(sentences.isOnlyMine());
		cbMine.setOnCheckedChangeListener(this);
		showLabel();
	}

	private void showLabel() {
		findViewById(R.id.tvNoSentences).setVisibility(
				sentences.size() == 0 ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listViewSentences) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			mActiveSentence = info.position;
			String sentence = sentences.get(mActiveSentence);
			menu.setHeaderTitle(sentence);

			menu.add(Menu.NONE, menuDeleteSentence, 0, R.string.delete_sentence);
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE
				|| actionId == EditorInfo.IME_ACTION_DONE
				|| (event.getAction() == KeyEvent.ACTION_DOWN && event
						.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
			addSentence(v.getText().toString());
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			v.setText("");

			return true;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == menuDeleteSentence) {
			sentences.remove(mActiveSentence);
			try {
				sentences.save(this);
			} catch (IOException e) {
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			}
			adapter.notifyDataSetChanged();
			showLabel();
		}
		return super.onOptionsItemSelected(item);
	}

	private void addSentence(String text) {

		if (text.length() > 0) {
			sentences.add(text);

			try {
				sentences.save(this);
			} catch (IOException e) {
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			}
			adapter.notifyDataSetChanged();
			showLabel();
			listView.smoothScrollToPosition(sentences.indexOf(text));
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		sentences.setOnlyMine(isChecked);
		try {
			sentences.save(this);
		} catch (IOException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}

	}
}
