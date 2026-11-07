package ru.ivorontsov.otuchenie;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(245, 241, 234);
    private static final int CARD = Color.WHITE;
    private static final int DARK = Color.rgb(28, 48, 45);
    private static final int MUTED = Color.rgb(105, 116, 110);
    private static final int ACCENT = Color.rgb(217, 104, 80);
    private static final int GOLD = Color.rgb(224, 173, 92);
    private static final int PALE = Color.rgb(237, 231, 220);
    private static final int GREEN = Color.rgb(50, 105, 85);

    private SharedPreferences prefs;
    private LinearLayout root;
    private LinearLayout navigation;
    private FrameLayout contentHost;
    private int currentTab = 0;
    private String focus = "both";
    private long timerEndMs = 0L;
    private TextView timerValue;
    private TextView consequenceValue;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable timerTick = new Runnable() {
        @Override
        public void run() {
            updateTimerText();
            if (timerEndMs > System.currentTimeMillis() && timerValue != null) {
                handler.postDelayed(this, 1000L);
            }
        }
    };

    private final String[] gamblingConsequences = {
        "АРЕНДА НЕ ПРИНИМАЕТ «ЕЩЁ ОДИН СПИН»\n\nЕсли деньги на жильё уходят в ставки, под угрозой оказывается базовая стабильность. Отдели деньги на жильё и еду от доступа к игре.",
        "ОТЫГРЫШ — ЭТО ЕЩЁ ОДНА СТАВКА\n\nПроигрыш уже произошёл. Следующая ставка не возвращает прошлое и может увеличить потери. Самый сильный ход сейчас — остановиться.",
        "СОН НЕ ПРИНИМАЕТ ПЛАТУ В БОНУСАХ\n\nНочная игра забирает сон, а утром вместе с ним внимание и силы. Поставь телефон на зарядку в другой комнате.",
        "ДОВЕРИЕ НЕ ВОССТАНАВЛИВАЕТСЯ КОЭФФИЦИЕНТОМ\n\nСекреты и долги могут разрушать отношения. Один честный разговор с безопасным человеком — конкретный шаг.",
        "ПРОИГРАННОЕ ВРЕМЯ НЕ ДАЁТ КЭШБЭК\n\nЧасы, проведённые в ставках, могли уйти на отдых, работу, учёбу или близких. Выбери одно маленькое дело на ближайшие 15 минут.",
        "КРЕДИТ НЕ ДЕЛАЕТ ШАНСЫ ЛУЧШЕ\n\nЗаём ради игры добавляет обязательства к уже случившимся потерям. Сегодня не занимай и не бери микрокредит.",
        "«ПОСЛЕДНИЙ РАЗ» НЕ ЯВЛЯЕТСЯ ПЛАНОМ\n\nОбещание остановиться после следующей ставки оставляет решение внутри того же импульса. Поставь между собой и игрой реальный барьер."
    };

    private final String[] gamingConsequences = {
        "ЕЩЁ ОДИН МАТЧ — И УТРО НАЧНЁТСЯ БЕЗ ТЕБЯ\n\nНедосып снижает концентрацию и настроение. Выйти из игры сегодня — не проигрыш, а сохранение ресурса.",
        "КВЕСТ «ПРОСРОЧЕННЫЕ ДЕЛА»\n\nКогда игра вытесняет учёбу, работу и быт, последствия копятся офлайн. Выбери одну маленькую задачу на 10 минут.",
        "СОЦИАЛЬНАЯ ЖИЗНЬ НЕ В ПАТЧ-НОУТАХ\n\nЕсли встречи и близкие постоянно откладываются ради сессии, отношения могут ослабнуть. Напиши одному человеку.",
        "ТЕЛО НЕ ПОЛУЧАЕТ XP ЗА НЕПОДВИЖНОСТЬ\n\nДолгое сидение и пропущенные перерывы усиливают усталость. Встань, пройдись и выпей воды.",
        "АВТОПРОДОЛЖЕНИЕ — НЕ ПРИКАЗ\n\nИгра или сервис может подталкивать к длинным сессиям. Решение закончить принадлежит тебе. Поставь время выхода до запуска.",
        "СВОБОДНОЕ ВРЕМЯ — НЕ БЕСКОНЕЧНЫЙ РЕСУРС\n\nКогда игра занимает каждый свободный промежуток, другим интересам не остаётся места. Запланируй одно занятие вне экрана."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("recovery_local", MODE_PRIVATE);
        focus = prefs.getString("focus", "both");
        timerEndMs = prefs.getLong("timer_end", 0L);

        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        contentHost = new FrameLayout(this);
        root.addView(contentHost, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ));

        navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER);
        navigation.setPadding(dp(6), dp(7), dp(6), dp(7));
        navigation.setBackgroundColor(Color.WHITE);
        navigation.setElevation(dp(9));
        root.addView(navigation, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(66)
        ));

        setContentView(root);
        buildNavigation();
        render();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit().putLong("timer_end", timerEndMs).apply();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(timerTick);
        super.onDestroy();
    }

    private void render() {
        handler.removeCallbacks(timerTick);
        timerValue = null;
        consequenceValue = null;
        contentHost.removeAllViews();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(22), dp(20), dp(28));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        if (currentTab == 0) {
            buildHome(page);
        } else if (currentTab == 1) {
            buildPractice(page);
        } else if (currentTab == 2) {
            buildProtection(page);
        } else {
            buildKnowledge(page);
        }

        contentHost.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        buildNavigation();
    }

    private void buildNavigation() {
        navigation.removeAllViews();
        String[] labels = {"Домой", "Практика", "Защита", "Знания"};
        String[] symbols = {"⌂", "◷", "⬡", "◎"};
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            TextView symbol = new TextView(this);
            symbol.setText(symbols[i]);
            symbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            symbol.setGravity(Gravity.CENTER);
            symbol.setTextColor(currentTab == i ? ACCENT : MUTED);
            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            label.setTypeface(Typeface.DEFAULT, currentTab == i ? Typeface.BOLD : Typeface.NORMAL);
            label.setTextColor(currentTab == i ? DARK : MUTED);
            item.addView(symbol);
            item.addView(label);
            item.setOnClickListener(v -> {
                currentTab = index;
                render();
            });
            navigation.addView(item, new LinearLayout.LayoutParams(0, dp(52), 1f));
        }
    }

    private void buildHome(LinearLayout page) {
        addText(page, "ОТУЧЕНИЕ  /  ЛИЧНЫЙ ПЛАН", 11, ACCENT, true);
        addText(page, "Верни себе\nуправление.", 34, DARK, true);
        addText(page,
                "Не нужно обещать себе идеальную жизнь с понедельника. Достаточно сделать следующий безопасный шаг сегодня.",
                15, MUTED, false);

        LinearLayout urgent = card(page, DARK);
        addText(urgent, "КОГДА ТЯНЕТ ПРЯМО СЕЙЧАС", 11, GOLD, true);
        addText(urgent, "Тяга — это импульс, а не приказ.", 22, Color.WHITE, true);
        addText(urgent,
                "Не принимай решение в разгар желания. Дай себе 10 минут, смени обстановку и вернись к выбору позже.",
                14, Color.rgb(224, 230, 224), false);
        addButton(urgent, "Запустить паузу на 10 минут", ACCENT, Color.WHITE, () -> {
            currentTab = 1;
            render();
            startUrgeTimer();
        });

        LinearLayout focusCard = card(page, CARD);
        addText(focusCard, "С ЧЕМ РАБОТАЕМ", 17, DARK, true);
        addText(focusCard,
                "Выбери направление. Содержание практик и защитных шагов подстроится под него.",
                13, MUTED, false);
        LinearLayout focusRow = new LinearLayout(this);
        focusRow.setOrientation(LinearLayout.HORIZONTAL);
        focusRow.setGravity(Gravity.CENTER);
        focusCard.addView(focusRow, matchWrap());
        addFocusButton(focusRow, "Азартные игры", "gambling");
        addFocusButton(focusRow, "Видеоигры", "gaming");
        addFocusButton(focusRow, "Оба", "both");

        LinearLayout anti = card(page, CARD);
        addText(anti, "АНТИ-ДЖЕКПОТ", 11, ACCENT, true);
        addText(anti, "Увидеть цену импульса", 21, DARK, true);
        addText(anti,
                "Не рулетка и не имитация выигрыша. Это короткая карточка о реальных рисках и одном действии, которое можно сделать вместо игры.",
                14, MUTED, false);
        addButton(anti, "Открыть практику", DARK, Color.WHITE, () -> {
            currentTab = 1;
            render();
        });

        LinearLayout progress = card(page, CARD);
        addText(progress, "ТВОИ ШАГИ УЖЕ СЧИТАЮТСЯ", 11, GREEN, true);
        int urges = prefs.getInt("urge_sessions", 0);
        int notes = prefs.getInt("thought_notes", 0);
        addText(progress, urges + " пауз  ·  " + notes + " записей", 24, DARK, true);
        addText(progress,
                "Здесь нет серии, которую можно «обнулить». Если был срыв, это повод вернуться к плану, а не вычёркивать сделанное.",
                13, MUTED, false);

        addText(page, "Данные остаются на этом устройстве. Приложение работает без аккаунта и не отправляет записи в интернет.",
                12, MUTED, false);
    }

    private void addFocusButton(LinearLayout row, String label, String value) {
        boolean selected = focus.equals(value);
        TextView b = button(label, selected ? ACCENT : PALE, selected ? Color.WHITE : DARK, () -> {
            focus = value;
            prefs.edit().putString("focus", focus).apply();
            render();
        });
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        b.setPadding(dp(7), dp(12), dp(7), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        lp.setMargins(dp(2), dp(4), dp(2), dp(4));
        row.addView(b, lp);
    }

    private void buildPractice(LinearLayout page) {
        addText(page, "ПРАКТИКА", 11, ACCENT, true);
        addText(page, "Переждать импульс.\nВернуть выбор.", 29, DARK, true);
        addText(page,
                "Используй упражнения до игры, во время тяги или после сложного эпизода. Не нужно делать всё сразу.",
                14, MUTED, false);

        LinearLayout timerCard = card(page, DARK);
        addText(timerCard, "ПАУЗА / 10 МИНУТ", 11, GOLD, true);
        addText(timerCard, "Сначала пауза. Потом решение.", 21, Color.WHITE, true);
        timerValue = addText(timerCard, "10:00", 39, Color.WHITE, true);
        timerValue.setGravity(Gravity.CENTER);
        timerValue.setPadding(0, dp(8), 0, dp(8));
        addText(timerCard,
                "На эти 10 минут не нужно решать вопрос «навсегда». Просто не открывай игру и не делай ставку сейчас.",
                13, Color.rgb(225, 230, 225), false);
        LinearLayout timerRow = new LinearLayout(this);
        timerRow.setOrientation(LinearLayout.HORIZONTAL);
        timerCard.addView(timerRow, matchWrap());
        TextView start = button("Начать", ACCENT, Color.WHITE, this::startUrgeTimer);
        TextView cancel = button("Сбросить", Color.rgb(55, 74, 68), Color.WHITE, this::cancelUrgeTimer);
        timerRow.addView(start, new LinearLayout.LayoutParams(0, dp(48), 1f));
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        cancelLp.leftMargin = dp(8);
        timerRow.addView(cancel, cancelLp);
        if (timerEndMs > System.currentTimeMillis()) {
            handler.post(timerTick);
        } else {
            updateTimerText();
        }

        LinearLayout actions = card(page, CARD);
        addText(actions, "ПОКА ИДЁТ ПАУЗА", 11, GREEN, true);
        addText(actions, "Выбери одно простое действие", 19, DARK, true);
        addBullet(actions, "Выйди из приложения, сайта или комнаты, где обычно играешь.");
        addBullet(actions, "Отложи телефон и пройдись хотя бы пять минут.");
        addBullet(actions, "Напиши человеку, которому доверяешь.");
        addBullet(actions, "Выпей воды и назови вслух, что сейчас чувствуешь.");
        addText(actions, "Не нужно побеждать тягу силой. Нужно помочь себе не действовать автоматически.",
                13, MUTED, false);

        LinearLayout anti = card(page, CARD);
        addText(anti, "АНТИ-ДЖЕКПОТ", 11, ACCENT, true);
        addText(anti, "Неприятная правда вместо обещания выигрыша", 19, DARK, true);
        addText(anti,
                "Карточка не предсказывает твоё будущее. Она напоминает о цене риска и предлагает следующий защитный шаг.",
                13, MUTED, false);
        consequenceValue = addText(anti,
                "Нажми кнопку, когда хочется быстро вспомнить, ради чего ты решил остановиться.",
                16, DARK, true);
        consequenceValue.setPadding(0, dp(10), 0, dp(12));
        addButton(anti, "Показать следующую карточку", ACCENT, Color.WHITE, this::drawConsequence);

        if (!focus.equals("gaming")) {
            addMoneyCalculator(page);
        }
        if (!focus.equals("gambling")) {
            addTimeCalculator(page);
        }

        LinearLayout journal = card(page, CARD);
        addText(journal, "УПРАЖНЕНИЕ КПТ", 11, GREEN, true);
        addText(journal, "Поймай автоматическую мысль", 20, DARK, true);
        addText(journal,
                "Запиши ситуацию, мысль, которая подталкивает к игре, и более честный ответ. Не нужно убеждать себя, что всё прекрасно — достаточно увидеть альтернативу.",
                13, MUTED, false);
        EditText trigger = editField(journal, "Что произошло перед тягой?");
        EditText thought = editField(journal, "Какая мысль зовёт сыграть?");
        EditText alternative = editField(journal, "Что я могу ответить себе по фактам?");
        String last = prefs.getString("last_thought", "");
        if (!last.isEmpty()) {
            TextView saved = addText(journal, "ПОСЛЕДНЯЯ ЗАПИСЬ", 10, GREEN, true);
            TextView note = addText(journal, last, 12, MUTED, false);
            note.setMaxLines(5);
        }
        addButton(journal, "Сохранить запись на устройстве", DARK, Color.WHITE, () -> {
            String a = trigger.getText().toString().trim();
            String b = thought.getText().toString().trim();
            String c = alternative.getText().toString().trim();
            if (a.isEmpty() && b.isEmpty() && c.isEmpty()) {
                toast("Добавь хотя бы одну мысль — дневник не умеет читать телепатически.");
                return;
            }
            String entry = "Ситуация: " + (a.isEmpty() ? "—" : a)
                    + "\nМысль: " + (b.isEmpty() ? "—" : b)
                    + "\nОтвет: " + (c.isEmpty() ? "—" : c);
            prefs.edit()
                    .putString("last_thought", entry)
                    .putInt("thought_notes", prefs.getInt("thought_notes", 0) + 1)
                    .apply();
            toast("Запись сохранена локально.");
            render();
        });
    }

    private void addMoneyCalculator(LinearLayout page) {
        LinearLayout box = card(page, CARD);
        addText(box, "КАЛЬКУЛЯТОР ЦЕНЫ", 11, ACCENT, true);
        addText(box, "Сколько это за год?", 19, DARK, true);
        addText(box,
                "Введи сумму, которую обычно уходило на ставки за неделю. Это арифметическая оценка, не прогноз и не повод себя ругать.",
                13, MUTED, false);
        EditText amount = editField(box, "Сумма за неделю — в твоей валюте");
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        TextView result = addText(box, "", 16, DARK, true);
        addButton(box, "Посчитать за 52 недели", PALE, DARK, () -> {
            try {
                double weekly = Double.parseDouble(amount.getText().toString().replace(',', '.').trim());
                if (weekly < 0 || weekly > 1000000000000d) throw new NumberFormatException();
                result.setText("За 52 недели: примерно "
                        + NumberFormat.getNumberInstance(new Locale("ru", "RU")).format(weekly * 52d)
                        + " в той же валюте.");
            } catch (NumberFormatException ex) {
                result.setText("Введи корректную сумму. Калькулятор строгий, но не осуждающий.");
            }
        });
    }

    private void addTimeCalculator(LinearLayout page) {
        LinearLayout box = card(page, CARD);
        addText(box, "КАЛЬКУЛЯТОР ВРЕМЕНИ", 11, ACCENT, true);
        addText(box, "Куда уходят часы?", 19, DARK, true);
        addText(box,
                "Введи примерное количество часов в неделю, которое игра вытесняет из других дел. Это не оценка тебя — только способ увидеть масштаб.",
                13, MUTED, false);
        EditText hours = editField(box, "Часов за неделю");
        hours.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        TextView result = addText(box, "", 16, DARK, true);
        addButton(box, "Посчитать за год", PALE, DARK, () -> {
            try {
                double weekly = Double.parseDouble(hours.getText().toString().replace(',', '.').trim());
                if (weekly < 0 || weekly > 168) throw new NumberFormatException();
                result.setText("За 52 недели: примерно "
                        + NumberFormat.getNumberInstance(new Locale("ru", "RU")).format(weekly * 52d)
                        + " часов.");
            } catch (NumberFormatException ex) {
                result.setText("Введи число от 0 до 168. Даже у недели есть лимиты.");
            }
        });
    }

    private void buildProtection(LinearLayout page) {
        addText(page, "ЗАЩИТА", 11, ACCENT, true);
        addText(page, "Сделай срыв\nсложнее.", 30, DARK, true);
        addText(page,
                "Одной силы воли мало. Убери часть триггеров и добавь несколько практических барьеров. Отмечай только то, что действительно сделал.",
                14, MUTED, false);

        if (!focus.equals("gaming")) {
            String[] gambling = {
                "Подать заявление на самоисключение у операторов и в доступных национальных системах.",
                "Узнать в своём банке о блокировке платежей в азартные сервисы.",
                "Установить блокировщик сайтов и приложений с азартными играми.",
                "Удалить сохранённые карты, отключить быстрые платежи и убрать кредитные лимиты для игры.",
                "Отключить рекламные рассылки, уведомления и подписки азартных сервисов.",
                "Попросить доверенного человека помочь с добровольным финансовым барьером."
            };
            addProtectionChecklist(page, "АЗАРТНЫЕ ИГРЫ", gambling, "gambling");
        }

        if (!focus.equals("gambling")) {
            String[] gaming = {
                "Отключить игровые push-уведомления, автозапуск и напоминания о событиях.",
                "Убрать игру с первого экрана или временно удалить наиболее проблемную игру.",
                "Включить системные лимиты приложений и заранее определить время завершения.",
                "Заряжать телефон и другие устройства вне спальни.",
                "Запланировать альтернативное занятие на обычное время игрового сеанса.",
                "Договориться с близким человеком о поддержке без контроля и унижения."
            };
            addProtectionChecklist(page, "ВИДЕОИГРЫ", gaming, "gaming");
        }

        LinearLayout lapse = card(page, DARK);
        addText(lapse, "ЕСЛИ СЛУЧИЛСЯ СРЫВ", 11, GOLD, true);
        addText(lapse, "Не превращай один эпизод в неделю.", 21, Color.WHITE, true);
        addText(lapse,
                "1. Остановись сейчас — не пытайся отыграться и не делай «последнюю» ставку.\n"
                + "2. Не занимай деньги ради возврата потерь.\n"
                + "3. Закрой доступ и включи один из защитных барьеров.\n"
                + "4. Свяжись с человеком, которому доверяешь.\n"
                + "5. Запиши, что стало триггером, и вернись к плану.\n\n"
                + "Срыв не означает, что всё потеряно. Это сигнал усилить поддержку.",
                14, Color.rgb(229, 233, 227), false);

        LinearLayout support = card(page, CARD);
        addText(support, "ПОПРОСИТЬ О ПОДДЕРЖКЕ", 11, GREEN, true);
        addText(support, "Не обязательно справляться в одиночку.", 19, DARK, true);
        addText(support,
                "Можно отправить близкому короткое сообщение без объяснений на десять страниц.",
                13, MUTED, false);
        addButton(support, "Скопировать сообщение", GREEN, Color.WHITE, () -> {
            String message = "Мне сейчас трудно остановиться. Можешь побыть со мной 15 минут без осуждения и помочь сделать доступ к игре сложнее?";
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Сообщение о поддержке", message));
            toast("Сообщение скопировано. Выбери безопасного человека.");
        });

        LinearLayout urgent = card(page, Color.rgb(255, 239, 233));
        addText(urgent, "ВАЖНО О БЕЗОПАСНОСТИ", 11, ACCENT, true);
        addText(urgent,
                "Если после проигрыша или из-за игровой зависимости появились мысли причинить себе вред или не хочется жить, не оставайся один. Свяжись с близким человеком, кризисной службой или местной экстренной службой. При непосредственной опасности обратись за экстренной помощью сейчас.",
                14, DARK, false);
    }

    private void addProtectionChecklist(LinearLayout page, String title, String[] items, String key) {
        LinearLayout box = card(page, CARD);
        addText(box, title, 11, GREEN, true);
        addText(box, "Маленькие барьеры работают вместе.", 18, DARK, true);
        TextView progress = addText(box, protectionProgress(items.length, key), 12, MUTED, false);
        for (int i = 0; i < items.length; i++) {
            final int index = i;
            CheckBox check = new CheckBox(this);
            check.setText(items[i]);
            check.setTextColor(DARK);
            check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            check.setButtonTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{GREEN, MUTED}
            ));
            check.setPadding(0, dp(5), 0, dp(5));
            check.setChecked(prefs.getBoolean("protect_" + key + "_" + i, false));
            box.addView(check, matchWrap());
            check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("protect_" + key + "_" + index, isChecked).apply();
                progress.setText(protectionProgress(items.length, key));
            });
        }
    }

    private String protectionProgress(int total, String key) {
        int done = 0;
        for (int i = 0; i < total; i++) {
            if (prefs.getBoolean("protect_" + key + "_" + i, false)) done++;
        }
        return "Выполнено: " + done + " из " + total;
    }

    private void buildKnowledge(LinearLayout page) {
        addText(page, "ЗНАНИЯ", 11, ACCENT, true);
        addText(page, "Мозг любит обещания.\nПроверим факты.", 29, DARK, true);
        addText(page,
                "Короткие материалы помогают замечать ловушки мышления. Они не заменяют консультацию специалиста и не ставят диагноз.",
                14, MUTED, false);

        addLesson(page, "01  /  ОШИБКА ИГРОКА",
                "«Давно не выпадало — значит, скоро должно».",
                "Нет. В независимом случайном розыгрыше прошлые результаты не заставляют следующий исход стать выигрышным. История проигрышей не создаёт долг перед тобой.");
        addLesson(page, "02  /  ПОГОНЯ ЗА ПОТЕРЯМИ",
                "«Нужно отыграться, и тогда остановлюсь».",
                "Попытка вернуть уже потерянное новой ставкой добавляет свежий риск. Прошлые потери — уже факт; следующую ставку можно не делать.");
        addLesson(page, "03  /  ИЛЛЮЗИЯ КОНТРОЛЯ",
                "«Я чувствую момент» или «у меня есть система».",
                "Уверенность, ритуалы и выбор времени не превращают случайный исход в управляемый. Система может казаться убедительной и всё равно не менять вероятность.");
        addLesson(page, "04  /  ЛОЖНЫЙ БЛИЗКИЙ ВЫИГРЫШ",
                "«Почти получилось — значит, я рядом».",
                "Почти-выигрыш не является выигрышем и не доказывает, что следующий раунд будет успешным. Он может только усилить желание продолжать.");
        addLesson(page, "05  /  ИГРЫ И УТРАТА КОНТРОЛЯ",
                "«Любить видеоигры — уже зависимость?»",
                "Нет. ВОЗ описывает игровое расстройство через нарушение контроля, рост приоритета игры над другими делами и продолжение несмотря на вред. Для диагноза требуется выраженное нарушение жизненного функционирования; обычно признаки наблюдаются не менее 12 месяцев.");
        addLesson(page, "06  /  СРЫВ — НЕ ПРИГОВОР",
                "«Раз сорвался, значит, всё бесполезно».",
                "Нет. В рекомендациях NICE рецидив описывается не как личная неудача, а как сигнал разобрать триггеры, усилить защиту и быстро вернуться к помощи.");

        LinearLayout sources = card(page, CARD);
        addText(sources, "НАУЧНЫЕ ИСТОЧНИКИ", 11, GREEN, true);
        addText(sources, "На чём основаны материалы приложения", 19, DARK, true);
        addText(sources,
                "• NICE NG248 — рекомендации по выявлению и лечению вреда от азартных игр (2025).\n"
                + "• ВОЗ — Gambling, информационный материал (2024).\n"
                + "• ВОЗ — Gaming disorder, определение по МКБ-11.\n"
                + "• Pfund et al., Addiction (2023) — систематический обзор и метаанализ КПТ при азартных проблемах.\n"
                + "• Harpas et al., Psychiatry Research (2025) — систематический обзор и метаанализ лечения игрового расстройства.",
                13, MUTED, false);
        addButton(sources, "Открыть рекомендации NICE", PALE, DARK,
                () -> openUrl("https://www.nice.org.uk/guidance/ng248/chapter/recommendations"));
        addButton(sources, "Открыть материалы ВОЗ об азартных играх", PALE, DARK,
                () -> openUrl("https://www.who.int/news-room/fact-sheets/detail/gambling"));
        addButton(sources, "Открыть определение ВОЗ для видеоигр", PALE, DARK,
                () -> openUrl("https://www.who.int/news-room/questions-and-answers/item/addictive-behaviours-gaming-disorder"));
        addButton(sources, "Открыть метаанализ КПТ (2023)", PALE, DARK,
                () -> openUrl("https://pubmed.ncbi.nlm.nih.gov/37381589/"));
        addButton(sources, "Открыть метаанализ лечения gaming disorder (2025)", PALE, DARK,
                () -> openUrl("https://pubmed.ncbi.nlm.nih.gov/41187384/"));

        addText(page,
                "Приложение — инструмент самопомощи, а не медицинская диагностика и не замена специалисту. При выраженных последствиях, долгах, депрессии или повторных срывах стоит обратиться за профессиональной помощью.",
                12, MUTED, false);
    }

    private void addLesson(LinearLayout page, String kicker, String headline, String body) {
        LinearLayout box = card(page, CARD);
        addText(box, kicker, 10, ACCENT, true);
        addText(box, headline, 17, DARK, true);
        addText(box, body, 13, MUTED, false);
    }

    private void drawConsequence() {
        if (consequenceValue == null) return;
        int shown = prefs.getInt("consequence_cards", 0);
        boolean useGambling = focus.equals("gambling")
                || (focus.equals("both") && shown % 2 == 0);
        String key = useGambling ? "gambling" : "gaming";
        String[] deck = useGambling ? gamblingConsequences : gamingConsequences;
        int index = prefs.getInt("anti_index_" + key, 0);
        String cardText = deck[index % deck.length];
        consequenceValue.setText(cardText);
        prefs.edit()
                .putInt("anti_index_" + key, index + 1)
                .putInt("consequence_cards", shown + 1)
                .apply();
    }

    private void startUrgeTimer() {
        timerEndMs = System.currentTimeMillis() + 10L * 60L * 1000L;
        prefs.edit()
                .putLong("timer_end", timerEndMs)
                .putInt("urge_sessions", prefs.getInt("urge_sessions", 0) + 1)
                .apply();
        updateTimerText();
        handler.removeCallbacks(timerTick);
        handler.postDelayed(timerTick, 1000L);
        toast("Пауза началась. На эти 10 минут решение отложено.");
    }

    private void cancelUrgeTimer() {
        timerEndMs = 0L;
        prefs.edit().putLong("timer_end", 0L).apply();
        handler.removeCallbacks(timerTick);
        updateTimerText();
    }

    private void updateTimerText() {
        if (timerValue == null) return;
        long remaining = timerEndMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            timerEndMs = 0L;
            prefs.edit().putLong("timer_end", 0L).apply();
            timerValue.setText("10:00");
            if (currentTab == 1) {
                timerValue.setText("Пауза завершена");
            }
            handler.removeCallbacks(timerTick);
            return;
        }
        long seconds = remaining / 1000L;
        long minutesPart = seconds / 60L;
        long secondsPart = seconds % 60L;
        timerValue.setText(String.format(Locale.getDefault(), "%02d:%02d", minutesPart, secondsPart));
    }

    private void addBullet(LinearLayout parent, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        TextView dot = new TextView(this);
        dot.setText("•");
        dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        dot.setTextColor(ACCENT);
        dot.setPadding(0, 0, dp(9), 0);
        row.addView(dot);
        TextView body = new TextView(this);
        body.setText(value);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        body.setTextColor(DARK);
        body.setLineSpacing(dp(2), 1f);
        row.addView(body, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams lp = matchWrap();
        lp.bottomMargin = dp(6);
        parent.addView(row, lp);
    }

    private EditText editField(LinearLayout parent, String hint) {
        EditText edit = new EditText(this);
        edit.setHint(hint);
        edit.setTextColor(DARK);
        edit.setHintTextColor(MUTED);
        edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        edit.setGravity(Gravity.TOP | Gravity.START);
        edit.setPadding(dp(13), dp(12), dp(13), dp(12));
        edit.setBackground(roundDrawable(Color.rgb(247, 245, 240), dp(13), Color.rgb(230, 225, 215), dp(1)));
        edit.setMinLines(2);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.bottomMargin = dp(9);
        parent.addView(edit, lp);
        return edit;
    }

    private void addButton(LinearLayout parent, String label, int background, int foreground, Runnable action) {
        TextView b = button(label, background, foreground, action);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(5);
        lp.bottomMargin = dp(5);
        parent.addView(b, lp);
    }

    private TextView button(String label, int background, int foreground, Runnable action) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextColor(foreground);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(13), dp(14), dp(13));
        b.setMinHeight(dp(46));
        b.setBackground(roundDrawable(background, dp(14), background, 0));
        b.setClickable(true);
        b.setFocusable(true);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private LinearLayout card(LinearLayout parent, int color) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(17), dp(17), dp(17), dp(17));
        box.setBackground(roundDrawable(color, dp(20), color, 0));
        box.setElevation(dp(1));
        LinearLayout.LayoutParams lp = matchWrap();
        lp.bottomMargin = dp(14);
        parent.addView(box, lp);
        return box;
    }

    private TextView addText(LinearLayout parent, String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        text.setTextColor(color);
        text.setLineSpacing(dp(2), 1f);
        if (bold) text.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        else text.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        LinearLayout.LayoutParams lp = matchWrap();
        lp.bottomMargin = dp(7);
        parent.addView(text, lp);
        return text;
    }

    private GradientDrawable roundDrawable(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()
        );
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception ex) {
            toast("Не удалось открыть ссылку. Проверь подключение к интернету.");
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
