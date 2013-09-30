package perassoft.multiplicationtables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {

	private static final String TTS = "TTS";
	private static final String SCORE = "score";
	private static final String KO = "2";
	private static final String OK = "1";
	private static final int RESULT_SPEECH_CHECK_CODE = 0;
	private static final int RESULT_SETTINGS = 1;
	private static final String A = "a";
	private static final String B = "b";
	private static final String ANSWERS = "answers";
	private int a;
	private int b;
	private TextToSpeech tts;
	private List<Integer> tables;
	private int maxButtons = 5;
	private View.OnClickListener yesClickListener;
	private OnClickListener noClickListener;
	private List<Button> buttons = new ArrayList<Button>();
	private Random random;
	private int score = 0;
	private AnswerList answers;
	private boolean questionNeeded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RESULT_SPEECH_CHECK_CODE);
		random = new Random(System.currentTimeMillis());
		setDifficulties();
		yesClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				score++;
				StringBuilder sb = new StringBuilder();
				sb.append(getString(R.string.right));
				sb.append(" ");
				sb.append(a);
				sb.append(" x ");
				sb.append(b);
				sb.append(" = ");
				sb.append(a * b);
				message(sb.toString(), OK);
				updateScoreView();
			}

		};

		noClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				score -= 2;
				message(getString(R.string.wrong), KO);
				updateScoreView();
			}

		};

		if (savedInstanceState != null) {
			score = savedInstanceState.getInt(SCORE, 0);
			a = savedInstanceState.getInt(A);
			b = savedInstanceState.getInt(B);
			answers = (AnswerList) savedInstanceState.getSerializable(ANSWERS);
			questionNeeded = false;
			setQuestionText();
			generateButtons();
		} else {
			questionNeeded = true;
			answers = new AnswerList();
		}

		updateScoreView();

	}

	private void updateScoreView() {
		TextView scoreText = (TextView) findViewById(R.id.textViewScore);
		scoreText.setText(String.format(getString(R.string.score), score));

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings :
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivityForResult(intent, RESULT_SETTINGS);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void message(String text, String messageId) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		if (tts != null) {
			HashMap<String, String> params = new HashMap<String, String>();

			params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, messageId);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
					WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
		} else
			onSpeechEnded(messageId);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(SCORE, score);
		outState.putInt(A, a);
		outState.putInt(B, b);
		outState.putSerializable(ANSWERS, answers);
		super.onSaveInstanceState(outState);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_SPEECH_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, this);

			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		} else if (requestCode == RESULT_SETTINGS) {
			setDifficulties();
			generateQuestion();
		} 
	}

	private void setDifficulties() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		tables = new ArrayList<Integer>();
		maxButtons = Integer.parseInt(sharedPrefs.getString("answer_number", "4"));
		for (int i = 1; i < 11; i++)
			if (sharedPrefs.getBoolean(i + "_checkbox", true))
				tables.add(i);
	}

	private void generateQuestion() {
		if (tables.size() == 0)
		{
			Toast.makeText(this, R.string.select_table, Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, RESULT_SETTINGS);
			return;
		}
		int index = random.nextInt(tables.size());
		a = tables.get(index);
		b = random.nextInt(10) + 1;
		String question = setQuestionText();

		if (tts != null) {
			tts.speak(question, TextToSpeech.QUEUE_ADD, null);
		}

		generateAnswers();
		generateButtons();
	}

	private String setQuestionText() {
		TextView textQuestion = (TextView) findViewById(R.id.textViewQuestion);
		String question = a + " x " + b + " ?";
		textQuestion.setText(question);
		return question;
	}

	private void generateAnswers() {
		int rightIdx = random.nextInt(maxButtons);

		List<Integer> numbers = new ArrayList<Integer>();
		numbers.add(a * b);
		answers.clear();
		for (int i = 0; i < maxButtons; i++) {
			boolean isRight = i == rightIdx;
			answers.add(new Answer(i == rightIdx, isRight
					? a * b
					: getWrongAnswer(numbers)));
		}
	}

	private void generateButtons() {
		PredicateLayout rl = (PredicateLayout) findViewById(R.id.answerContainer);
		for (Button b : buttons)
			rl.removeView(b);

		for (int i = 0; i < answers.size(); i++) {
			Answer answer = answers.get(i);
			Button btn = new Button(this);
			PredicateLayout.LayoutParams lp = new PredicateLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			//lp.leftMargin = lp.rightMargin = 2;
			// lp.addRule(RelativeLayout.RIGHT_OF, id);

			btn.setText(answer.getResponse().toString());
			rl.addView(btn, lp);
			btn.setOnClickListener(answer.isCorrect()
					? yesClickListener
					: noClickListener);
			buttons.add(btn);
		}
	}

	private int getWrongAnswer(List<Integer> answers) {

		int answer = 0;
		do {
			switch (random.nextInt(3)) {
				case 0 :
					answer = a * b + random.nextInt(20) - 10;
					break;
				case 1 :
					answer = (a + random.nextInt(4) - 2) * b;
					break;
				case 2 :
					answer = (b + random.nextInt(4) - 2) * a;
					break;
			}
			answer = Math.max(1, answer);
			answer = Math.min(100, answer);
		} while (answers.contains(answer));
		answers.add(answer);
		return answer;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		// TTS is successfully initialized
		if (status == TextToSpeech.SUCCESS) {
			// Setting speech language
			Locale current = getResources().getConfiguration().locale;
			int result = tts.setLanguage(current);
			tts.setPitch(1.9f);
			tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

				@Override
				public void onUtteranceCompleted(String utteranceId) {
					final String id = utteranceId;
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							onSpeechEnded(id);
						}

					});

				}
			});
			// If your device doesn't support language you set above
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Cook simple toast message with message
				Toast.makeText(this, R.string.language_not_supported,
						Toast.LENGTH_LONG).show();
				Log.e(TTS, getString(R.string.language_not_supported));
			}

			// TTS is not initialized properly
		} else {
			Toast.makeText(this, R.string.tts_initilization_failed,
					Toast.LENGTH_LONG).show();
			Log.e(TTS, getString(R.string.tts_initilization_failed));
		}
		if (questionNeeded)
			generateQuestion();

	}

	private void onSpeechEnded(String utteranceId) {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		if (utteranceId.equals(OK))
			generateQuestion();

	}

	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
}
