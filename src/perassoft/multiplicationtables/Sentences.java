package perassoft.multiplicationtables;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;

public class Sentences extends ArrayList<String> {

	private boolean onlyMine;
	private static final String SENTENCES_BIN = "sentences.bin";
	/**
	 * 
	 */
	private static final long serialVersionUID = 5232648094390766379L;
	private static Sentences sentences;

	public static Sentences getSentences(Context context) {
		if (sentences == null) {
			if (context.getFileStreamPath(SENTENCES_BIN)
					.exists())
				sentences = (Sentences) Helper.readObject(
						context, SENTENCES_BIN);
			if (sentences == null)
				sentences = new Sentences();
		}
		return sentences;
	}
	
	public void save(Context context) throws IOException
	{
		Helper.saveObject(context, SENTENCES_BIN, this);
	}

	public boolean isOnlyMine() {
		return onlyMine;
	}

	public void setOnlyMine(boolean onlyMine) {
		this.onlyMine = onlyMine;
	}
	
	
}