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

package edu.vub.at.nfcpoker.ui.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;

/**
 * Bitmap provider.
 */
public class PageProvider implements CurlView.PageProvider {

	// Bitmap resources.
	private int[] mBitmapIds;
	private Context ctx;

	public PageProvider(Context ctx, int[] mBitmapIds) {
		this.ctx = ctx;
		this.mBitmapIds = mBitmapIds;
	}

	@Override
	public int getPageCount() {
		return mBitmapIds.length;
	}
	
	private Bitmap loadBitmap(int width, int height, int index) {
		Bitmap b = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		// b.eraseColor(Color.WHITE);
		Canvas c = new Canvas(b);
		Drawable d = ctx.getResources().getDrawable(mBitmapIds[index]);

		int margin = 7;
		int border = 3;
		Rect r = new Rect(margin, margin, width - margin, height - margin);

		int imageWidth = r.width() - (border * 2);
		int imageHeight = imageWidth * d.getIntrinsicHeight()
				/ d.getIntrinsicWidth();
		if (imageHeight > r.height() - (border * 2)) {
			imageHeight = r.height() - (border * 2);
			imageWidth = imageHeight * d.getIntrinsicWidth()
					/ d.getIntrinsicHeight();
		}

		r.left += ((r.width() - imageWidth) / 2) - border;
		r.right = r.left + imageWidth + border + border;
		r.top += ((r.height() - imageHeight) / 2) - border;
		r.bottom = r.top + imageHeight + border + border;

		Paint p = new Paint();
		p.setColor(0xFFC0C0C0); // Lode
		//p.setColor(Color.WHITE);
		c.drawRect(r, p);
		r.left += border;
		r.right -= border;
		r.top += border;
		r.bottom -= border;

		d.setBounds(r);
		d.draw(c);

		return b;
	}

	@Override
	public void updatePage(CurlPage page, int width, int height, int index) {

		switch (index) {
			// First case is image on front side, solid colored back.
			case 0: {
				Bitmap front = loadBitmap(width, height, 0);
				Bitmap back = loadBitmap(width, height, 0);
				page.setTexture(front, CurlPage.SIDE_FRONT);
				page.setTexture(back, CurlPage.SIDE_BACK);
				break;
			}
			// Second case is image on back side, solid colored front.
			case 1: {
				Bitmap front = loadBitmap(width, height, 1);
				page.setTexture(front, CurlPage.SIDE_FRONT);
				break;
			}
		}
	}
}

