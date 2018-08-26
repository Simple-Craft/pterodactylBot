/*
 * PterodactylBot - An open-source Discord integration for Pterodactyl
 * Copyright (C) 2018  Michael Rittmeister
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package me.schlaubi.pterodactylbot.core.translation;

import me.schlaubi.pterodactylbot.PterodactylBot;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TranslationManager {

    private final Logger logger = Logger.getLogger(TranslationManager.class);

    private final List<Locale> locales;
    private final Locale defaultLocale;

    public TranslationManager() {
        defaultLocale = new Locale(this, new java.util.Locale("en", "US"), "English (United States)") {
            @Override
            public String translate(String key) {
                if (getResourceBundle().containsKey(key))
                    return getResourceBundle().getString(key);
                else {
                    logger.error(String.format("TranslationLocale for '%s' missing in default locale %s", key, defaultLocale.getLanguageName()));
                    return "Missing translation.";
                }
            }
        };

        locales = new ArrayList<>();
        locales.add(defaultLocale);
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    private Locale getLocaleByLocale(java.util.Locale locale) {
        return locales.parallelStream().filter(locale1 -> locale1.getLocale().equals(locale)).collect(Collectors.toList()).get(0);
    }

    public Locale getLocaleByUser(String userId) {
        String languageTag = null;
        try {
            PreparedStatement ps = PterodactylBot.getInstance().getMySQL().getConnection().prepareStatement("SELECT language FROM users WHERE userId = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                languageTag = rs.getString("language");
            if (languageTag == null)
                languageTag = "en_US";
        } catch (SQLException e) {
            logger.error(String.format("[TRANSLATONMANAGER] There was an error while fetching locale for user %s", userId), e);
            languageTag = "en_US";
        }
        try {
            String[] tag = languageTag.split("_");
            java.util.Locale locale = new java.util.Locale(tag[0], tag[1]);
            return getLocaleByLocale(locale);
        } catch (Exception e2) {
            logger.error(String.format("[TRANSLATONMANAGER] There was an error while fetching locale for tag", languageTag), e2);
        }
        return null;
    }
}
