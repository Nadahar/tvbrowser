/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package blogthisplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 *
 */
final class BlogSettings extends PropertyBasedSettings {

  private static final String KEY_BLOG_URL = "BlogUrl";
  private static final String KEY_BLOG_SERVICE = "BlogService";
  /** Service Names */
  private static final String BLOGGER = "BLOGGER";
  private static final String WORDPRESS = "WORDPRESS";
  private static final String B2EVOLUTION = "B2EVOLUTION";

  BlogSettings(Properties settings) {
    super(settings);
  }

  String getBlogUrl() {
    return getBlogUrl(null);
  }

  void setService(final BlogService service) {
    if (service == null) {
      remove(KEY_BLOG_SERVICE);
      return;
    }
    switch (service) {
      case Blogger: {
        set(KEY_BLOG_SERVICE, BlogSettings.BLOGGER);
        break;
      }
      case WordPress: {
        set(KEY_BLOG_SERVICE, BlogSettings.WORDPRESS);
        break;
      }
      case B2Evolution: {
        set(KEY_BLOG_SERVICE, BlogSettings.B2EVOLUTION);
        break;
      }
      default: {
      	remove(KEY_BLOG_SERVICE);
      }
    }
  }

  BlogService getBlogService() {
    String service = get(KEY_BLOG_SERVICE, "");
    if (service.equals(BlogSettings.BLOGGER)) {
      return BlogService.Blogger;
    }
    else if (service.equals(BlogSettings.WORDPRESS)) {
      return BlogService.WordPress;
    }
    else if (service.equals(BlogSettings.B2EVOLUTION)) {
      return BlogService.B2Evolution;
    }
    return null;
  }

  void setBlogUrl(final String url) {
    set(KEY_BLOG_URL, url);
  }

  String getBlogUrl(final String defaultUrl) {
    return get(KEY_BLOG_URL, defaultUrl);
  }




}