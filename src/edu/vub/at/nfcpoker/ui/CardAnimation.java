/*
 * wePoker: Play poker with your friends, wherever you are!
 * Copyright (C) 2012, The AmbientTalk team.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.vub.at.nfcpoker.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.widget.ImageButton;

public class CardAnimation {
	static boolean isHoneyComb = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
	private static ICardAnimation cardInstance = null;

	static public void setCardImage(ImageButton ib, int drawable) {
		getCardAnimation().setCardImage(ib, drawable);
	}

	private static ICardAnimation getCardAnimation() {
		if (cardInstance == null) {
			cardInstance = isHoneyComb ? new CardAnimationHC() : new CardAnimationGB();
		}
		return cardInstance;
	}
	
	
	private interface ICardAnimation {
		public void setCardImage(ImageButton ib, int drawable);
	}
	
	public static class CardAnimationGB implements ICardAnimation {

		@Override
		public void setCardImage(ImageButton ib, int drawable) {
			ib.setImageResource(drawable);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class CardAnimationHC implements ICardAnimation {

		@Override
		public void setCardImage(final ImageButton ib, final int drawable) {
			ObjectAnimator animX = ObjectAnimator.ofFloat(ib, "scaleX", 1.f, 0.f);
			ObjectAnimator animY = ObjectAnimator.ofFloat(ib, "scaleY", 1.f, 0.f);
			animX.setDuration(500); animY.setDuration(500);
			final AnimatorSet scalers = new AnimatorSet();
			scalers.play(animX).with(animY);
			scalers.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					ib.setScaleX(1.f);
					ib.setScaleY(1.f);
					ib.setImageResource(drawable);
				}

			});
			scalers.start();
		}
	}
}
