/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.game.subsystem.mode;

import java.util.Random;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

/**
 * CHALLENGER Mode
 */
public class ChallengerMode extends DummyMode {
	/** Current version */
	private static final int CURRENT_VERSION = 3;
	
	/** Default torikan time for non-classic rules */
	private static final int DEFAULT_TORIKAN = 13920;

	/** Default torikan time for classic rules */
	private static final int DEFAULT_TORIKAN_CLASSIC = 13200;

	/** Section COOL criteria Time */
	private static final int[] tableTimeCool =
	{
		3120, 3120, 2940, 2700, 2700, 2520, 2520, 2280, 2280, 2280, 1980, 1980, 1980, 1680, 1680, 1320, 1080, 840, 540, 360, -1
	};

	/** Section REGRET criteria Time */
	private static final int[] tableTimeRegret =
	{
		5400, 4500, 4500, 4080, 3600, 3600, 3000, 3000, 3000, 3000, 2700, 2700, 2700, 2400, 2400, 2040, 1800, 1560, 1020, 480, 300
	};
	/** 落下速度 table */
	private static final int[] tableGravityValue =
	{
		4, 16, 64, 256, 1024,-1
	};

	/** 落下速度 table */
	private static final int[] newTableGravityValue =
	{
		5, 20, 80, 320, 1280,-1
	};

	/** 落下速度が変わる level */
	private static final int[] tableGravityChangeLevel =
	{
		0, 100, 200, 300, 400, 10000
	};

	private static final int[] tableGarbage   = { 0,  0,  20,  20,  20, 20, 20, 20,  20,  20,  16,  16,  16,  8, 8, 6, 5, 4, 3, 2, 2, 1, 1, 1, 1, 1};
	
	/** ARE table */
	private static final int[] tableARE       = {25, 25, 25, 25, 25, 20,16,13,10, 8, 6, 5, 4, 4, 3, 3, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	/** ARE after line clear table */
	private static final int[] tableARELine   = {10, 10, 10, 10, 10,  8, 6, 5, 4, 3, 3, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	/** Line clear time table */
	private static final int[] tableLineDelay = {15, 15, 15, 15, 15, 12,10, 8, 6, 5, 4, 3, 3, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0};

	/** 固定 time table */
	private static final int[] tableLockDelay = {31, 31, 31, 31, 31, 25,20,16, 13, 11,  9, 7, 6, 5, 4, 4, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2};

	/** DAS table */
	private static final int[] tableDAS       = {15, 15, 15, 15, 15, 9,  9, 9,  9,  6,  6, 6, 6, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};

	/** BGM fadeout levels */
	private static final int[] tableBGMFadeout = {385,585,680,860,950,-1};

	/** BGM change levels */
	private static final int[] tableBGMChange  = {400,600,700,900,1000,-1};
	/** BGM change levels in settings */
	private static final int[] tableBGMChangeMenu = {0,0,0,0,1,1,2,3,3,4,5,5,5,5,5,5,5,5,5,5,5,5,11,11,11};
	/** BGM change levels in settings */
	private static final int[] tableBGMChangeMenu20g = {1,1,2,3,3,4,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,11,11,11};

	/** Line clear時に入る段位 point */
	private static final int[] tableGradePoint =
	{
		10,
		30,
		60,
		120
	};

	/** 段位 pointのCombo bonus */
	private static final float[][] tableGradeComboBonus =
	{
		{1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f},
		{1.0f,1.2f,1.2f,1.4f,1.4f,1.4f,1.4f,1.5f,1.5f,2.0f},
		{1.0f,1.4f,1.5f,1.6f,1.7f,1.8f,1.9f,2.0f,2.1f,2.5f},
		{1.0f,1.5f,1.8f,2.0f,2.2f,2.3f,2.4f,2.5f,2.6f,3.0f},
	};

	/** 実際の段位を上げるのに必要な内部段位 */
	private static final int[] tableGradeChange =
	{
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, -1
	};
	/** 実際の段位を上げるのに必要な内部段位 */
	private static final int[] tableGradeChangeClassic =
	{
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 
		40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76,
		77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, -1
	};

	/** 段位 pointが1つ減る time */
	private static final int[] tableGradeDecayRate =
	{
		125, 80, 80, 50, 45, 45, 45, 40, 40, 40, 40, 40, 30, 30, 30, 20, 20, 20, 20, 20, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
	};

	/** 段位のName */
	private static final String[] tableGradeName =
	{
		 "9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0～ 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9～17
		 "GM", "FPGM", "0N 0H FPGM", "FURTH FPGM", "FR0N0HFPGM"	// 18～22
	};
	
	private static final String[] tableGradeNameClassic =
	{
		"9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0～ 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9～17
		"M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "M",	//  18～27
		"MK", "MK1","MK2","MK3","MK4","MK5","MK6","MK7","MK8","MK9",	//  28～37
		"MJ", "MJ1","MJ2","MJ3","MJ4","MJ5","MJ6","MJ7","MJ8","MJ9",	//  38～47
		"MV", "MV1","MV2","MV3","MV4","MV5","MV6","MV7","MV8","MV9",	//  48～57
		"MO", "MO1","MO2","MO3","MO4","MO5","MO6","MO7","MO8","MO9",	//  58～67
		"MM", "MM1","MM2","MM3","MM4","MM5","MM6","MM7","MM8","MM9",	//  68～77
		"GM","GM1","GM2","GM3","GM4","GM5","GM6","GM7","GM8",	//  78～86
		"GM9","GMK","GMJ","GMV","GMO","GMM", "FPGM", "0N 0H FPGM", //  87～94
		"FURTH FPGM", "FR0N0HFPGM"								//	95-96
	};

	/** 裏段位のName */
	private static final String[] tableSecretGradeName =
	{
		 "9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0～ 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9～17
		"GM"													// 18
	};

	/** LV999 roll time */
	private static final int ROLLTIMELIMIT = 6600;

	/** 消えRoll に必要なLV999到達時のTime */
	private static final int M_ROLL_TIME_REQUIRE = 70000;

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of ranking types */
	private static final int RANKING_TYPE = 2;

	/** ж®µдЅЌе±Ґж­ґгЃ®г‚µг‚¤г‚є */
	private static final int GRADE_HISTORY_SIZE = 7;

	/** ж®µдЅЌе±Ґж­ґгЃ®г‚µг‚¤г‚є */
	private static final int CLASSIC_GRADE_HISTORY_SIZE = 7;

	/** ж®µдЅЌиЄЌе®љи©¦йЁ“гЃ®з™єз”џзўєзЋ‡(EXAM_CHANCEе€†гЃ®1гЃ®зўєзЋ‡гЃ§з™єз”џ) */
	private static final int EXAM_CHANCE = 3;
	
	/** Number of sections */
	private static final int SECTION_MAX = 21;

	/** Default section time */
	private static final int DEFAULT_SECTION_TIME = 5400;

	/** GameManager that owns this mode */
	private GameManager owner;

	/** Drawing and event handling EventReceiver */
	private EventReceiver receiver;

	/** Current 落下速度の number (tableGravityChangeLevelの levelに到達するたびに1つ増える) */
	private int gravityindex;

	/** Next Section の level (これ-1のときに levelストップする) */
	private int nextseclv;

	/** Levelが増えた flag */
	private boolean lvupflag;

	/** 画面に表示されている実際の段位 */
	private int grade;

	/** 内部段位 */
	private int gradeInternal;

	/** 段位 point */
	private long gradePoint;
	
	private long statGradePoints;

	/** 段位 pointが1つ減る time */
	private int gradeDecay;

	/** 最後に段位が上がった time */
	private int lastGradeTime;

	/** Hard dropした段count */
	private int harddropBonus;

	/** Combo bonus */
	private int comboValue;

	/** Most recent increase in score */
	private int lastscore;

	/** 獲得Render scoreがされる残り time */
	private int scgettime;

	/** гЃ“гЃ®Section гЃ§COOLг‚’е‡єгЃ™гЃЁtrue */
	private boolean cool;

	/** COOL count */
	private int coolcount;

	/** з›ґе‰ЌгЃ®Section гЃ§COOLг‚’е‡єгЃ—гЃџг‚‰true */
	private boolean previouscool;

	/** з›ґе‰ЌгЃ®Section гЃ§гЃ® level70йЂљйЃЋTime */
	private int coolprevtime;

	/** гЃ“гЃ®Section гЃ§COOL check г‚’гЃ—гЃџг‚‰true */
	private boolean coolchecked;

	/** гЃ“гЃ®Section гЃ§COOLиЎЁз¤єг‚’гЃ—гЃџг‚‰true */
	private boolean cooldisplayed;

	/** COOL display time frame count */
	private int cooldispframe;

	/** REGRET display time frame count */
	private int regretdispframe;

	/** COOL section flags*/
	private boolean[] coolsection;

	/** REGRET section flags*/
	private boolean[] regretsection;
	
	/** Roll 経過 time */
	private int rolltime;

	/** Roll completely cleared flag */
	private int rollclear;

	/** Roll started flag */
	private boolean rollstarted;

	/** 裏段位 */
	private int secretGrade;

	/** Current BGM */
	private int bgmlv;

	/** 段位表示を光らせる残り frame count */
	private int gradeflash;

	/** Section Time */
	private int[] sectiontime;

	private boolean[] sectionIsNewRecord;

	/** Cleared Section count */
	private int sectionscomp;

	/** Average Section Time */
	private int sectionavgtime;

	/** 直前のSection Time */
	private int sectionlasttime;

	/** Section 内で4-line clearた count */
	private int[] sectionfourline;

	/** 消えRoll  flag１ (Section Time) */
	private boolean mrollSectiontime;

	/** 消えRoll  flag２ (4-line clear) */
	private boolean mrollFourline;

	/** 消えRoll started flag */
	private boolean mrollFlag;

	/** 消えRoll 中に消したline count */
	private int mrollLines;

	/** AC medal 状態 */
	private int medalAC;

	/** ST medal 状態 */
	private int medalST;

	/** SK medal 状態 */
	private int medalSK;

	/** RE medal 状態 */
	private int medalRE;

	/** RO medal 状態 */
	private int medalRO;

	/** CO medal 状態 */
	private int medalCO;
	
	private int garbageCount;

	/** 150個以上Blockがあるとtrue, 70個まで減らすとfalseになる */
	private boolean recoveryFlag;

	/** rotationした合計 count (Maximum4個ずつ増える) */
	private int rotateCount;

	private boolean isShowBestSectionTime;

	/** Level at start */
	private int startlevel;

	/** When true, always ghost ON */
	private boolean alwaysghost;

	/** When true, always 20G */
	private boolean always20g;

	/** When true, levelstop sound is enabled */
	private boolean lvstopse;

	/** BigMode */
	private boolean big;
	
	/** LV500гЃ®и¶іе€‡г‚ЉTime */
	private int torikan;

	/** When true, section time display is enabled */
	private boolean showsectiontime;
	
	/** ж�‡ж јгѓ»й™Ќж ји©¦йЁ“ is enabled */
	private boolean enableexam;
	
	private int dtetlives;

	/** Version */
	private int version;

	/** Current round's ranking rank */
	private int rankingRank;

	/** Rankings' 段位 */
	private int[][] rankingGrade;

	/** Rankings'  level */
	private int[][] rankingLevel;

	/** Rankings' times */
	private int[][] rankingTime;

	/** Rankings' Roll completely cleared flag */
	private int[][] rankingRollclear;
	
	/** Current round's ranking rank */
	private int rankingClassicRank;

	/** Rankings' 段位 */
	private int[][] rankingClassicGrade;

	/** Rankings'  level */
	private int[][] rankingClassicLevel;

	/** Rankings' times */
	private int[][] rankingClassicTime;

	/** Rankings' Roll completely cleared flag */
	private int[][] rankingClassicRollclear;

	private int rankingRank20g;
	
	/** Rankings' 段位 */
	private int[][] rankingGrade20g;

	/** Rankings'  level */
	private int[][] rankingLevel20g;

	/** Rankings' times */
	private int[][] rankingTime20g;

	/** Rankings' Roll completely cleared flag */
	private int[][] rankingRollclear20g;
	
	private int rankingClassicRank20g;
	
	/** Rankings' 段位 */
	private int[][] rankingClassicGrade20g;

	/** Rankings'  level */
	private int[][] rankingClassicLevel20g;

	/** Rankings' times */
	private int[][] rankingClassicTime20g;

	/** Rankings' Roll completely cleared flag */
	private int[][] rankingClassicRollclear20g;

	private int[][] bestSectionTime;

	/** ж®µдЅЌе±Ґж­ґ (ж�‡ж јгѓ»й™Ќж ји©¦йЁ“з”Ё) */
	private int[] gradeHistory;

	/** ж®µдЅЌе±Ґж­ґ (ж�‡ж јгѓ»й™Ќж ји©¦йЁ“з”Ё) */
	private int[] classicGradeHistory;

	/** ж�‡ж ји©¦йЁ“гЃ®з›®жЁ™ж®µдЅЌ */
	private int promotionalExam;
	
	private int classicPromotionalExam;

	/** Current иЄЌе®љж®µдЅЌ */
	private int qualifiedGrade;
	
	private int qualifiedClassicGrade;

	/** й™Ќж ји©¦йЁ“ point (30д»ҐдёЉжєњгЃѕг‚‹гЃЁй™Ќж ји©¦йЁ“з™єз”џ) */
	private int demotionPoints;

	/** й™Ќж ји©¦йЁ“ point (30д»ҐдёЉжєњгЃѕг‚‹гЃЁй™Ќж ји©¦йЁ“з™єз”џ) */
	private int classicDemotionPoints;

	/** ж�‡ж ји©¦йЁ“ flag */
	private boolean promotionFlag;

	/** ж�‡ж ји©¦йЁ“ flag */
	private boolean classicPromotionFlag;

	/** й™Ќж ји©¦йЁ“ flag */
	private boolean demotionFlag;

	/** й™Ќж ји©¦йЁ“гЃ§гЃ®з›®жЁ™ж®µдЅЌ */
	private int demotionExamGrade;

	/** й™Ќж ји©¦йЁ“ flag */
	private boolean classicDemotionFlag;

	/** й™Ќж ји©¦йЁ“гЃ§гЃ®з›®жЁ™ж®µдЅЌ */
	private int classicDemotionExamGrade;

	/** и©¦йЁ“й–‹е§‹е‰Ќжј”е‡єгЃ® frame count */
	private int readyframe;

	/** и©¦йЁ“зµ‚дє†жј”е‡єгЃ® frame count */
	private int passframe;

	public double gradescorereq;
	public float gradepointpps;
	private int garbageDelay;
	private int virtualBasePoint;
	private boolean challengerGameOver;
	private boolean initgameover;
	private int normaltime;
	private int confignpieces;
	private int gradeincreasedelay;
	private int gradeincreaseamount;
	private int tspintime;
	private int modetype;
	private boolean furth;
	private int speed_level;
	private int login0arrwarningtime;
	private boolean AIused;
	private boolean lvlroll;
	private boolean started;
	private boolean musicinitialised;
	private int nextpieces20genforced;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "CHALLENGER";
	}

	
	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;

		engine.ruleopt.holdEnable = true;
		engine.ruleopt.lockresetMove = true;
		engine.ruleopt.lockresetFall = true;
		engine.ruleopt.lockresetRotate = true;
		engine.ruleopt.pieceEnterAboveField = true;
		engine.ruleopt.fieldHiddenHeight = 20;
		engine.ruleopt.lockresetLimitMove = 15;
		engine.ruleopt.lockresetLimitRotate = 9;
		engine.staffrollEnableStatistics = true;
		musicinitialised = false;
		nextpieces20genforced = 100;
		if (engine.ai != null && owner.replayMode == false) AIused = true;
		else if (owner.replayMode == false) AIused = false;
		lvlroll = false;
		statGradePoints = 0;
		normaltime = 0;
		furth = false;
		login0arrwarningtime = 300;
		tspintime = 0;
		gradeincreasedelay = 0;
		gradeincreaseamount = 0;
		challengerGameOver = false;
		initgameover = false;
		gravityindex = 0;
		garbageCount = 0;
		garbageDelay = 0;
		gradescorereq = 100;
		nextseclv = 0;
		lvupflag = true;
		grade = 0;
		gradeInternal = 0;
		gradePoint = 0;
		gradeDecay = 0;
		lastGradeTime = 0;
		harddropBonus = 0;
		comboValue = 0;
		lastscore = 0;
		scgettime = 0;
		cool = false;
		coolcount = 0;
		previouscool = false;
		coolprevtime = 0;
		coolchecked = false;
		cooldisplayed = false;
		cooldispframe = 0;
		regretdispframe = 0;
		started = false;
		rolltime = 0;
		rollclear = 0;
		rollstarted = false;
		secretGrade = 0;
		bgmlv = 0;
		gradeflash = 0;
		sectiontime = new int[SECTION_MAX];
		sectionIsNewRecord = new boolean[SECTION_MAX];
		regretsection = new boolean[SECTION_MAX];
		coolsection = new boolean[SECTION_MAX];
		sectionscomp = 0;
		sectionavgtime = 0;
		sectionlasttime = 0;
		sectionfourline = new int[SECTION_MAX];
		mrollSectiontime = true;
		mrollFourline = true;
		mrollFlag = false;
		mrollLines = 0;
		medalAC = 0;
		medalST = 0;
		medalSK = 0;
		medalRE = 0;
		medalRO = 0;
		medalCO = 0;
		recoveryFlag = false;
		rotateCount = 0;
		isShowBestSectionTime = false;
		startlevel = 0;
		alwaysghost = false;
		always20g = false;
		lvstopse = false;
		big = false;
		enableexam = false;
		promotionalExam = 0;
		classicPromotionalExam = 0;
		qualifiedGrade = 0;
		qualifiedClassicGrade = 0;
		demotionPoints = 0;
		readyframe = 0;
		passframe = 0;
		gradeHistory = new int[GRADE_HISTORY_SIZE];
		classicGradeHistory = new int[CLASSIC_GRADE_HISTORY_SIZE];
		promotionFlag = false;
		classicPromotionFlag = false;
		demotionFlag = false;
		demotionExamGrade = 0;
		classicDemotionFlag = false;
		classicDemotionExamGrade = 0;

		if(modetype >= 2 && modetype < 4) enableexam = true;
		rankingRank = -1;
		rankingGrade = new int[RANKING_MAX][RANKING_TYPE];
		rankingLevel = new int[RANKING_MAX][RANKING_TYPE];
		rankingTime = new int[RANKING_MAX][RANKING_TYPE];
		rankingRollclear = new int[RANKING_MAX][RANKING_TYPE];
		rankingRank20g = -1;
		rankingGrade20g = new int[RANKING_MAX][RANKING_TYPE];
		rankingLevel20g = new int[RANKING_MAX][RANKING_TYPE];
		rankingTime20g = new int[RANKING_MAX][RANKING_TYPE];
		rankingRollclear20g = new int[RANKING_MAX][RANKING_TYPE];
		bestSectionTime = new int[SECTION_MAX][RANKING_TYPE];
		rankingClassicRank = -1;
		rankingClassicGrade = new int[RANKING_MAX][RANKING_TYPE];
		rankingClassicLevel = new int[RANKING_MAX][RANKING_TYPE];
		rankingClassicTime = new int[RANKING_MAX][RANKING_TYPE];
		rankingClassicRollclear = new int[RANKING_MAX][RANKING_TYPE];
		rankingClassicRank20g = -1;
		rankingClassicGrade20g = new int[RANKING_MAX][RANKING_TYPE];
		rankingClassicLevel20g = new int[RANKING_MAX][RANKING_TYPE];
		rankingClassicTime20g = new int[RANKING_MAX][RANKING_TYPE];
		rankingClassicRollclear20g = new int[RANKING_MAX][RANKING_TYPE];
		bestSectionTime = new int[SECTION_MAX][RANKING_TYPE];

		engine.tspinEnable = true;
		engine.b2bEnable = false;
		engine.comboType = GameEngine.COMBO_TYPE_DOUBLE;
		engine.bighalf = true;
		engine.bigmove = true;
		engine.staffrollEnable = true;
		engine.staffrollNoDeath = false;

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig, engine.ruleopt.strRuleName);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			for(int i = 0; i < SECTION_MAX; i++) {
				bestSectionTime[i][enableexam ? 1 : 0] = DEFAULT_SECTION_TIME;
			}
			loadSetting(owner.replayProp, engine.ruleopt.strRuleName);
			version = owner.replayProp.getProperty("challenger.version", 0);
			AIused = owner.replayProp.getProperty("challenger.aitype", false);
			if(enableexam) {
				promotionalExam = owner.replayProp.getProperty("challenger.exam", 0);
				demotionPoints = owner.replayProp.getProperty("challenger.demopoint", 0);
				demotionExamGrade = owner.replayProp.getProperty("challenger.demotionExamGrade", 0);
				classicPromotionalExam = owner.replayProp.getProperty("challenger.classicexam", 0);
				classicDemotionPoints = owner.replayProp.getProperty("challenger.classicdemopoint", 0);
				classicDemotionExamGrade = owner.replayProp.getProperty("challenger.classicdemotionExamGrade", 0);
					if (classicPromotionalExam > 0) {
						classicPromotionFlag = true;
						readyframe = 100;
						passframe = 600;
					}
					else if (demotionPoints >= 30) {
						classicDemotionFlag = true;
						passframe = 600;
					}
					if (promotionalExam > 0) {
						promotionFlag = true;
						readyframe = 100;
						passframe = 600;
					}
					else if (demotionPoints >= 30) {
						demotionFlag = true;
						passframe = 600;
					}

			}
		}
		if (engine.getDASDelay() == 0 || (engine.ai != null || AIused)) { engine.playSE("danger");engine.playSE("danger");engine.playSE("danger"); engine.playSE("garbage");}

		if(always20g && startlevel > (20 - confignpieces) && startlevel < 20) {
			engine.ruleopt.nextDisplay = 19 - startlevel;
			nextpieces20genforced = 19 - startlevel;
		}
		else if (always20g && startlevel >= 20) { engine.ruleopt.nextDisplay = 0; nextpieces20genforced = 0;}
		if(startlevel > 19) owner.backgroundStatus.bg = 19;
		else owner.backgroundStatus.bg = startlevel;
		owner.bgmStatus.volume = 1;
		owner.bgmStatus.bgm = -1;
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop, String strRuleName) {
		startlevel = prop.getProperty("challenger.startlevel", 0);
		alwaysghost = prop.getProperty("challenger.alwaysghost", false);
		always20g = prop.getProperty("challenger.always20g", false);
		lvstopse = prop.getProperty("challenger.lvstopse", true);
		showsectiontime = prop.getProperty("challenger.showsectiontime", false);
		dtetlives = prop.getProperty("challenger.lives", 1);
		confignpieces = prop.getProperty("challenger.nextpieces." + strRuleName, 3);
		big = prop.getProperty("challenger.big", false);
		int defaultTorikan = DEFAULT_TORIKAN;
		if(strRuleName.contains("CLASSIC")) defaultTorikan = DEFAULT_TORIKAN_CLASSIC;
		torikan = prop.getProperty("challenger.torikan." + strRuleName, defaultTorikan);
		modetype = prop.getProperty("challenger.type." + strRuleName, 0);
		furth = prop.getProperty("challenger.furthest", false);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop, String strRuleName) {
		prop.setProperty("challenger.startlevel", startlevel);
		prop.setProperty("challenger.alwaysghost", alwaysghost);
		prop.setProperty("challenger.always20g", always20g);
		prop.setProperty("challenger.lvstopse", lvstopse);
		prop.setProperty("challenger.showsectiontime", showsectiontime);
		prop.setProperty("challenger.lives", dtetlives);
		prop.setProperty("challenger.nextpieces." + strRuleName, confignpieces);
		prop.setProperty("challenger.big", big);
		prop.setProperty("challenger.torikan." + strRuleName, torikan);
		prop.setProperty("challenger.type." + strRuleName, modetype);
		prop.setProperty("challenger.furthest", furth);
	}

	/**
	 * Set BGM at start of game
	 * @param engine GameEngine
	 */
	private void setStartBgmlv(GameEngine engine) {
		if (always20g) { bgmlv = 1; while((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= (tableBGMChange[bgmlv]) -400)) bgmlv++;}
		else {bgmlv = 0;
		while((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= tableBGMChange[bgmlv])) bgmlv++; }
	}

	/**
	 * Update falling speed
	 * @param engine GameEngine
	 */
	private void setSpeed(GameEngine engine) {
		if(!furth)speed_level = engine.statistics.level;
		else if (engine.statistics.level < 1100 && version > 2) speed_level = engine.statistics.level + 1000;
		else speed_level = 2100;
		if((always20g == true) || (engine.statistics.time >= 36000)) {
			engine.speed.gravity = -1;
		} else {
			while(speed_level >= tableGravityChangeLevel[gravityindex]) gravityindex++;
			engine.speed.gravity = (version < 3) ? tableGravityValue[gravityindex] : newTableGravityValue[gravityindex]; 
		}
		int section = speed_level / 100;
		if(section > tableARE.length - 1) section = tableARE.length - 1;
		if(always20g) engine.speed.das = tableDAS[section+4];
		else if (engine.statistics.time >= 36000) engine.speed.das = 0;
		else engine.speed.das = tableDAS[section];

		if(engine.statistics.time >= 36000) {
			engine.speed.are = 0;
			engine.speed.areLine = 0;
			engine.speed.lineDelay = 0;
			engine.speed.lockDelay = 2;
		} else {
			if (always20g) {
				engine.speed.are = tableARE[section+4];
				engine.speed.areLine = tableARELine[section+4];
				engine.speed.lineDelay = tableLineDelay[section+4];
				engine.speed.lockDelay = tableLockDelay[section+4];
			}
			else {
				engine.speed.are = tableARE[section];
				engine.speed.areLine = tableARELine[section];
				engine.speed.lineDelay = tableLineDelay[section];
				engine.speed.lockDelay = tableLockDelay[section];
			}
		}
	}

	/**
	 * Update average section time
	 */
	private void setAverageSectionTime() {
		if(sectionscomp > 0) {
			int temp = 0;
			for(int i = startlevel; i < startlevel + sectionscomp; i++) {
				if((i >= 0) && (i < sectiontime.length)) temp += sectiontime[i];
			}
			sectionavgtime = temp / sectionscomp;
		} else {
			sectionavgtime = 0;
		}
	}

	/**
	 * 消えRoll 条件を満たしているか check
	 * @param levelb 上がる前の level
	 */
	private void mrollCheck(int levelb) {

		// 4-line clear
		int required4line = 2;
		if((levelb >= 500) && (levelb < 900)) required4line = 1;
		if(levelb >= 2000) required4line = 0;

		if(sectionfourline[levelb / 100] < required4line) {
			mrollFourline = false;
		}
	}

	/**
	 * ST medal check
	 * @param engine GameEngine
	 * @param sectionNumber Section number
	 */
	private void stMedalCheck(GameEngine engine, int sectionNumber) {
		int type = enableexam ? 1 : 0;
		int best = bestSectionTime[sectionNumber][type];

		if(sectionlasttime < best) {
			if(medalST < 3) {
				engine.playSE("medal");
				medalST = 3;
			}
			if(!owner.replayMode) {
				sectionIsNewRecord[sectionNumber] = true;
			}
		} else if((sectionlasttime < best + 300) && (medalST < 2)) {
			engine.playSE("medal");
			medalST = 2;
		} else if((sectionlasttime < best + 600) && (medalST < 1)) {
			engine.playSE("medal");
			medalST = 1;
		}
	}

	/**
	 * RO medal check
	 * @param engine Engine
	 */
	private void roMedalCheck(GameEngine engine) {
		float rotateAverage = (float)rotateCount / (float)engine.statistics.totalPieceLocked;

		if((rotateAverage >= 1.2f) && (medalRO < 3)) {
			engine.playSE("medal");
			medalRO++;
		}
	}

	/**
	 *  medal の文字色を取得
	 * @param medalColor  medal 状態
	 * @return  medal の文字色
	 */
	private int getMedalFontColor(int medalColor) {
		if(medalColor == 1) return EventReceiver.COLOR_RED;
		if(medalColor == 2) return EventReceiver.COLOR_WHITE;
		if(medalColor == 3) return EventReceiver.COLOR_YELLOW;
		if(medalColor == 4) return EventReceiver.COLOR_BLUE;
		return -1;
	}
	private void checkCool(GameEngine engine) {
		// COOL check
		if((engine.statistics.level % 100 >= 70) && (coolchecked == false && engine.statistics.level < 2001)) {
			int section = engine.statistics.level / 100;

			if( (sectiontime[section] <= tableTimeCool[section]) &&
				((previouscool == false) || ((previouscool == true) && (sectiontime[section] <= coolprevtime + 60))) )
			{
				cool = true;
				coolsection[section] = true;
			}
			else coolsection[section] = false;
			coolprevtime = sectiontime[section];
			coolchecked = true;
		}

		// COOLиЎЁз¤є
		if((engine.statistics.level % 100 >= 82) && (cool == true) && (cooldisplayed == false)) {
			engine.playSE("cool");
			cooldispframe = 180;
			cooldisplayed = true;
			virtualBasePoint += 600;
		}
	}

	/**
	 * REGRETгЃ® check
	 * @param engine GameEngine
	 * @param levelb Line clearе‰ЌгЃ® level
	 */
	private void checkRegret(GameEngine engine, int levelb) {
		int section = levelb / 100;
		if(sectionlasttime > tableTimeRegret[section]) {
			if(coolcount < 0) coolcount = 0;

			virtualBasePoint -= 600;

			regretdispframe = 180;
			engine.playSE("regret");;
			regretsection[section] = true;
		}
		else {
			regretsection[section] = false;
		}
	}
	/**
	 * @return дЅ•г‚‰гЃ‹гЃ®и©¦йЁ“дё­гЃЄг‚‰true
	 */
	private boolean isAnyExam() {
		if(modetype == 3) return classicPromotionFlag || classicDemotionFlag;
		else return promotionFlag || demotionFlag;
	}

	/**
	 * ж®µдЅЌеђЌг‚’еЏ–еѕ—
	 * @param g ж®µдЅЌ number
	 * @return ж®µдЅЌеђЌ(зЇ„е›Іе¤–гЃЄг‚‰N/A)
	 */
	private String getGradeName(int g) {
		if(modetype == 1 || modetype == 3)
		{
			if((g < 0) || (g >= tableGradeNameClassic.length)) return "N/A";
			return tableGradeNameClassic[g];
		}
		else
		{
			if((g < 0) || (g >= tableGradeName.length)) return "N/A";
			return tableGradeName[g];
		}
	}

	private void sendGarbage(GameEngine engine)
	{
		engine.playSE("garbage");
		if (engine.statistics.level > 600)
			engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GREEN,
					  engine.getSkin(),
					  Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE | Block.BLOCK_ATTRIBUTE_BONE,
					  1);
		else engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GRAY,
										  engine.getSkin(),
										  Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE,
										  1);
		if(engine.big) {
			if (engine.statistics.level > 600)
				engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GREEN,
						  engine.getSkin(),
						  Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE | Block.BLOCK_ATTRIBUTE_BONE,
						  1);
			else engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GRAY,
						engine.getSkin(),
						Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE,1);
		}
		garbageCount = 0;
	}
	private void sendGarbage20g(GameEngine engine)
	{
		engine.playSE("garbage");
		if (engine.statistics.level > 200)
			engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GREEN,
					  engine.getSkin(),
					  Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE | Block.BLOCK_ATTRIBUTE_BONE,
					  1);
		else engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GRAY,
										  engine.getSkin(),
										  Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE,
										  1);
		if(engine.big) {
			if (engine.statistics.level > 200)
				engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GREEN,
						  engine.getSkin(),
						  Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE | Block.BLOCK_ATTRIBUTE_BONE,
						  1);
			else engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GRAY,
						engine.getSkin(),
						Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE,1);
		}
		garbageCount = 0;
	}
	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(startlevel >= 6 && !always20g) engine.bone = true;
		else if(startlevel >= 2 & always20g) engine.bone = true;
		if(startlevel > 19) owner.backgroundStatus.bg = 19;
		else owner.backgroundStatus.bg = startlevel;
		if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 10);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
				case 0:
					startlevel += change;
					if(modetype < 4 &&startlevel < 0 ) startlevel = 21;
					if(modetype < 4 &&startlevel > 21) startlevel = 0;
					if(modetype == 4 && startlevel < 0) startlevel = 5;
					if(modetype == 4 && startlevel > 5) startlevel = 0;
					if (!furth && !always20g) owner.bgmStatus.bgm = tableBGMChangeMenu[startlevel];
					else if (always20g && !furth) owner.bgmStatus.bgm = tableBGMChangeMenu20g[startlevel];
					else owner.bgmStatus.bgm = 15;
					if (modetype == 4 && startlevel == 5) owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;
					if(always20g && startlevel > (20 - confignpieces) && startlevel < 20) {
						engine.ruleopt.nextDisplay = 19 - startlevel;
						nextpieces20genforced = 19 - startlevel;
					}
					else if (always20g && startlevel >= 20) { engine.ruleopt.nextDisplay = 0; nextpieces20genforced = 0;}
					if (confignpieces <= nextpieces20genforced || !always20g) engine.ruleopt.nextDisplay = confignpieces;
					else engine.ruleopt.nextDisplay = nextpieces20genforced;
					if(startlevel < 6) engine.bone = false;
					if(always20g && startlevel >= 20) engine.ruleopt.holdEnable = false;
					else engine.ruleopt.holdEnable = true;
					break;
				case 1:
					alwaysghost = !alwaysghost;
					break;
				case 2:
					always20g = !always20g;
					if(modetype == 4) always20g = false;
					if (!furth && !always20g) owner.bgmStatus.bgm = tableBGMChangeMenu[startlevel];
					else if (always20g && !furth) owner.bgmStatus.bgm = tableBGMChangeMenu20g[startlevel];
					else owner.bgmStatus.bgm = 15;
					if (modetype == 4 && startlevel == 5) owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;
					if(!always20g) nextpieces20genforced = 100;
					if(always20g && startlevel > (20 - confignpieces) && startlevel < 20) {
						nextpieces20genforced = 19 - startlevel;
					}
					else if (always20g && startlevel >= 20) { nextpieces20genforced = 0;}
					if(always20g && startlevel >= 20) engine.ruleopt.holdEnable = false;
					else engine.ruleopt.holdEnable = true;
					if (confignpieces <= nextpieces20genforced || !always20g) engine.ruleopt.nextDisplay = confignpieces;
					else engine.ruleopt.nextDisplay = nextpieces20genforced;
					break;
				case 3:
					lvstopse = !lvstopse;
					break;
				case 4:
					confignpieces += change;
					if(always20g && startlevel > (20 - confignpieces) && startlevel < 20) {
						engine.ruleopt.nextDisplay = 19 - startlevel;
						nextpieces20genforced = 19 - startlevel;
					}
					else if (always20g && startlevel >= 20) { engine.ruleopt.nextDisplay = 0; nextpieces20genforced = 0;}
					if (confignpieces < 0) confignpieces = 100;
					if (confignpieces > 100) confignpieces = 0;
					if (confignpieces <= nextpieces20genforced || !always20g) engine.ruleopt.nextDisplay = confignpieces;
					else engine.ruleopt.nextDisplay = nextpieces20genforced;
					break;
				case 5:
					showsectiontime = !showsectiontime;
					break;
				case 6:
					big = !big;
					break;
				case 7:
					dtetlives += change;
					if (dtetlives > 10) dtetlives = 1;
					if (dtetlives < 1) dtetlives = 10;
					break;
				case 8:
					torikan += 60 * change;
					if(torikan < 0) torikan = 72000;
					if(torikan > 72000) torikan = 0;
					break;
				case 9:
					modetype += change;
					if(modetype > 4) modetype = 0;
					if(modetype < 0) modetype = 4;
					if(modetype == 4 && startlevel > 5) startlevel = 5;
					if(modetype == 4) {always20g = false; furth = false; engine.bone = false;}
					if(modetype >= 2 && modetype < 4) enableexam = true;
					else enableexam = false;
					break;
				case 10:
					furth = !furth;
					if(modetype == 4) furth = false;
					if (!furth && !always20g) owner.bgmStatus.bgm = tableBGMChangeMenu[startlevel];
					else if (always20g && !furth) owner.bgmStatus.bgm = tableBGMChangeMenu20g[startlevel];
					else owner.bgmStatus.bgm = 15;
					if (modetype == 4 && startlevel == 5) owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;
					break;
				}
			}

			//  section time display切替
			if(engine.ctrl.isPush(Controller.BUTTON_F) && (engine.statc[3] >= 5)) {
				engine.playSE("change");
				isShowBestSectionTime = !isShowBestSectionTime;
			}

			// 決定
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				owner.bgmStatus.bgm = -1;
				saveSetting(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);

				isShowBestSectionTime = false;

				sectionscomp = 0;

				Random rand = new Random();
				if(!big && enableexam && (rand.nextInt(EXAM_CHANCE) == 0)) {
					setPromotionalGrade();
					if(modetype == 3)
					{
						if(classicPromotionalExam > qualifiedClassicGrade) {
							classicPromotionFlag = true;
							readyframe = 100;
							passframe = 600;
						} else if(classicDemotionPoints >= 30) {
							classicDemotionFlag = true;
							classicDemotionExamGrade = qualifiedClassicGrade;
							passframe = 600;
							classicDemotionPoints = 0;
						}
					}
					else {
						if(promotionalExam > qualifiedGrade) {
							promotionFlag = true;
							readyframe = 100;
							passframe = 600;
						} else if(demotionPoints >= 30) {
							demotionFlag = true;
							demotionExamGrade = qualifiedGrade;
							passframe = 600;
							demotionPoints = 0;
						}
					}
				}

				return false;
			}


			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
			}

			engine.statc[3]++;
		} else {
			engine.statc[3]++;
			engine.statc[2] = -1;

			if(engine.statc[3] >= 60) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Readyв†’GoгЃ®е‡¦зђ†
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if(promotionFlag || classicPromotionFlag) {
			engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;

			if(readyframe == 100) engine.playSE("tspin3");

			if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_B))
				readyframe = 0;

			if(readyframe > 0) {
				readyframe--;
				return true;
			}
		} else if(demotionFlag || classicDemotionFlag) {
			engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
			engine.playSE("danger");
		}
		if(((startlevel == 21 && modetype < 4) || (startlevel == 5 && modetype == 4)))
		{
			if(!started)
			engine.playSE("endingstart"); 
			started = true;
		}

		return false;
	}

	/*
	 * Readyв†’GoгЃ®гЃЁгЃЌгЃ®жЏЏз”»
	 */
	@Override
	public void renderReady(GameEngine engine, int playerID) {
		if((promotionFlag || classicPromotionFlag) && readyframe > 0) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "PROMOTION", EventReceiver.COLOR_YELLOW);
			receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_YELLOW);
			int x = (5 - getGradeName(promotionalExam).length() / 2);
			if(modetype == 3) {
				x = (5 - getGradeName(classicPromotionalExam).length() / 2);
			}
			if(modetype == 3) receiver.drawMenuFont(engine, playerID, x, 6, getGradeName(classicPromotionalExam), (readyframe % 4 ==0),
				EventReceiver.COLOR_WHITE, EventReceiver.COLOR_ORANGE);
			else receiver.drawMenuFont(engine, playerID, x, 6, getGradeName(promotionalExam), (readyframe % 4 ==0),
				EventReceiver.COLOR_WHITE, EventReceiver.COLOR_ORANGE);
		}
	}
	
	/*
	 * Render the settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_RED, 0,
				"LEVEL", (startlevel == 21 && modetype < 4) ? "ROLL" : (startlevel == 5 && modetype == 4) ? "ROLL": String.valueOf(startlevel * 100),
				"FULL GHOST", GeneralUtil.getONorOFF(alwaysghost),
				"20G MODE", (modetype == 4) ? "NO ACCESS" : GeneralUtil.getONorOFF(always20g),
				"LVSTOPSE", GeneralUtil.getONorOFF(lvstopse),
				"NEXT PIECE", (confignpieces > engine.ruleopt.nextDisplay) ? (String.valueOf(engine.ruleopt.nextDisplay) + " (" + String.valueOf(confignpieces) + ")") : String.valueOf(engine.ruleopt.nextDisplay),
				"SHOW STIME", GeneralUtil.getONorOFF(showsectiontime),
				"BIG",  GeneralUtil.getONorOFF(big),
				"LIVES", String.valueOf(dtetlives),
				"TORIKAN", (torikan == 0) ? "NONE" : GeneralUtil.getTime(torikan),
				"MODE TYPE", (modetype == 0) ? "STANDARD" : (modetype == 1) ? "CLASSIC" : (modetype == 2) ? "EXAM" : (modetype == 3) ? "EXAM CL" : "EASY");
		if(engine.stat == GameEngine.STAT_SETTING)
		{
			int xfont = (receiver.getNextDisplayType() == 2) ? 8 : 0;
			int yintr = 42;
			if (((engine.getDASDelay() > 0 && (engine.ai == null || !AIused)) || login0arrwarningtime == 0) && isShowBestSectionTime == false && owner.replayMode == false)receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr - 1, "DESCRIPTION:", 0.5f);
			if (engine.getDASDelay() == 0 && login0arrwarningtime > 0) { receiver.drawScoreFont(engine, playerID, -15 - xfont / 2, yintr / 2, "WARNING! 0 DAS DELAY OR ARR MAY CAUSE", EventReceiver.COLOR_RED);  receiver.drawScoreFont(engine, playerID, -15 - xfont / 2, yintr / 2 + 1, "MAJOR INCONVENIENCES LATER ON!", EventReceiver.COLOR_RED);}
			if ((engine.ai != null || AIused) && login0arrwarningtime > 0) { receiver.drawScoreFont(engine, playerID, -15 - xfont / 2, yintr / 2, "WARNING! BOT USAGE DETECTED!", EventReceiver.COLOR_RED);}
			else if (((engine.getDASDelay() > 0 && (engine.ai == null || !AIused)) || login0arrwarningtime == 0) && !isShowBestSectionTime && owner.replayMode == false) if (!receiver.isTTFSupport()) switch(engine.statc[2]) {
			case 0:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "THE STARTING LEVEL. SETTING IT TO ANYTHING OTHER THAN 0 WILL INVALIDATE", 0.5f);
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr + 1, "YOUR PLAY FROM THE RANKINGS. (MATTMAYUGA'S REWORD)", 0.5f);
				break;
			case 1:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "GHOST PIECE SETTING. WHEN IT'S ON, GHOST PIECE WILL STAY, EVEN ON 20G.", 0.5f);
				break;
			case 2:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "20G MODE. EQUVALENT TO INCREASING SPEED LEVEL BY 400. IT HAS OWN SEPARATE", 0.5f);
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr + 1, "LEADERBOARD.", 0.5f);
				break;
			case 3:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "LEVEL STOP SOUND EFFECT WILL PLAY AT **99 LEVEL WHEN IT'S ENABLED.", 0.5f);
				break;
			case 4:
				if (confignpieces > 8)	receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "WHAT'S WRONG WITH YOU!? HOW MUCH NEXT PIECES YOU SET!? " + confignpieces + " NEXT PIECES!?", 0.5f);
				else receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "NEXT PIECES SHOWN. HIGH LEVEL 20G NEXT PIECE CHANGING FEATURE IS ENFORCED.", 0.5f);
				break;
			case 5:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "SECTION TIME SETTING. IT WILL SHOW 15 OUT OF 21 SECTIONS. SHOWING ALL 21", 0.5f);
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr + 1, "SECTIONS IS A BIT DISTURBING.", 0.5f);
				break;
			case 6:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "BIG PIECES SETTING. IT'S SELF EXPLANATORY ONCE YOU PLAY WITH BIG PIECES ON.", 0.5f);
				break;
			case 7:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "AMOUNT OF LIVES. SETTING MORE THAN 1 LIFE DISABLES LEADERBOARD SAVING.", 0.5f);
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr + 1, "TOPPING OUT WITH 1 LIFE COUNTS AS GAME OVER.", 0.5f);
				break;
			case 8:
				int defaultTorikan = DEFAULT_TORIKAN;
				if(engine.ruleopt.strRuleName.contains("CLASSIC")) defaultTorikan = DEFAULT_TORIKAN_CLASSIC;
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "TORIKAN OR LEVEL 500 LIMIT. DEFAULT TORIKAN TIME IS " + GeneralUtil.getTime(defaultTorikan)+ ".", 0.5f);
				break;
			case 9:
				receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "MODE TYPE. STANDARD IS DEFAULT. FIND OUT WHAT OTHER TYPES DO.", 0.5f);
				break;
			case 10:
				if (furth) receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "YOU FOUND A DIFFICULTY EASTER EGG! ALSO KNOWN AS FURTHEST.", 0.5f);
				else receiver.drawScoreFont(engine, playerID, -30 - xfont, yintr, "WHERE'S YOUR CURSOR?", 0.5f);
				break;
			}
			else switch(engine.statc[2]) {
			case 0:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "The starting level. Setting it to anything other than 0 will invalidate");
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2 + 1, "your play from the rankings. (MattMayuga's reword)");
				break;
			case 1:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Ghost piece setting. When it's ON, ghost piece will stay, even on 20G.");
				break;
			case 2:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "20G mode. Equvalent to increasing speed level by 400. It has own separate");
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2 + 1, "leaderboard.");
				break;
			case 3:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Level stop sound effect will play at **99 level when it's enabled.");
				break;
			case 4:
				if (confignpieces > 8)	receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "What's wrong WITH YOU!? HOW MUCH NEXT PIECES YOU SET!? " + confignpieces + " NEXT PIECES!?");
				else receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Next pieces shown. High level 20G next piece changing feature is enforced.");
				break;
			case 5:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Section time setting. It will show 15 out of 21 sections. Showing all 21");
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2 + 1, "sections is a bit disturbing.");
				break;
			case 6:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "BIG pieces setting. It's self explanatory once you play with BIG pieces on.");
				break;
			case 7:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Amount of lives. Setting more than 1 life disables leaderboard saving.");
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2 + 1, "Topping out with 1 life counts as GAME OVER.");
				break;
			case 8:
				int defaultTorikan = DEFAULT_TORIKAN;
				if(engine.ruleopt.strRuleName.contains("CLASSIC")) defaultTorikan = DEFAULT_TORIKAN_CLASSIC;
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Torikan or Level 500 limit. Default torikan time is " + GeneralUtil.getTime(defaultTorikan)+ ".");
				break;
			case 9:
				receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Mode type. Standard is default. Find out what other types do.");
				break;
			case 10:
				if (furth) receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "You found a difficulty Easter Egg! Also known as Furthest.");
				else receiver.drawTTFScoreFont(engine, playerID, -15 - xfont, yintr /2, "Where's YOUR CURSOR?");
				break;
			}
		}
	}

	/*
	 * Called at game start
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = (startlevel == 21) ? 2100 : (startlevel * 100);
		engine.lives = dtetlives - 1;
		engine.ruleopt.nextDisplay = confignpieces;
		if(modetype >= 2 && modetype < 4) enableexam = true;
		login0arrwarningtime = 0;
		//if (startlevel == 21) calcScore(engine, playerID, 4);
		if(startlevel == 21 && modetype < 4) mrollFlag = true;
		
		if(owner.replayMode) owner.bgmStatus.bgm = -1;

		if(engine.statistics.level >= 600 && !always20g) engine.bone = true;
		else if (engine.statistics.level >= 200 && always20g) engine.bone = true;
		else engine.bone = false;
		nextseclv = engine.statistics.level + 100;
		if(engine.statistics.level < 0) nextseclv = 100;
		if(engine.statistics.level >= 2000 && modetype < 4) nextseclv = 2100;
		if(engine.statistics.level >= 400 && modetype == 4) nextseclv = 500;

		if(engine.statistics.level < 1900) owner.backgroundStatus.bg = engine.statistics.level / 100;
		else owner.backgroundStatus.bg = 19;
		engine.big = big;

		setSpeed(engine);
		setStartBgmlv(engine);
		if (engine.statistics.level < 1900) engine.ruleopt.holdEnable = true;
		if(always20g && engine.statistics.level > (20 - confignpieces) * 100 && engine.statistics.level < 2000) {
			engine.ruleopt.nextDisplay = 19 - (engine.statistics.level / 100);
		}
		else if (always20g && engine.statistics.level >= 2000) engine.ruleopt.nextDisplay = 0;
		if ((engine.statistics.level / 100) > 19 && always20g) { engine.ruleopt.holdEnable = false;}
		
		if(!furth)owner.bgmStatus.bgm = bgmlv;
		else owner.bgmStatus.bgm = BGMStatus.BGM_SPECIAL4;

	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if (furth)
		receiver.drawScoreFont(engine, playerID, 0, -1, "FURTHEST", EventReceiver.COLOR_RED);
		receiver.drawScoreFont(engine, playerID, 0, 0, "CHALLENGER", EventReceiver.COLOR_RED);
		receiver.drawScoreFont(engine, playerID, 11, 0, "V .", EventReceiver.COLOR_RED);
		receiver.drawScoreFont(engine, playerID, 12, 0, String.valueOf((version % 100) / 10), EventReceiver.COLOR_RED);
		receiver.drawScoreFont(engine, playerID, 14, 0, String.valueOf(version % 10), EventReceiver.COLOR_RED);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (startlevel == 0) && (big == false) && (engine.ai == null) && dtetlives == 1 && !furth && modetype < 4 && (login0arrwarningtime == 0 || engine.getDASDelay() > 0)) {
				if(!isShowBestSectionTime) {
					// Rankings
					float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
					int topY = (receiver.getNextDisplayType() == 2) ? 5 : 3;
					receiver.drawScoreFont(engine, playerID, 3, topY-1, "GRADE LEVEL TIME", EventReceiver.COLOR_BLUE, scale);

					for(int i = 0; i < RANKING_MAX; i++) {
						int type = enableexam ? 1 : 0;
						int gcolor = EventReceiver.COLOR_WHITE;
						if((rankingRollclear[i][type] == 1) || (rankingRollclear[i][type] == 3)) gcolor = EventReceiver.COLOR_GREEN;
						if((rankingRollclear[i][type] == 2) || (rankingRollclear[i][type] == 4)) gcolor = EventReceiver.COLOR_ORANGE;

						receiver.drawScoreFont(engine, playerID, 0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						if(always20g) {
							if(modetype == 1 || modetype == 3)
							{
								if((rankingClassicGrade20g[i][type] >= 0) && (rankingClassicGrade20g[i][type] < tableGradeNameClassic.length))
									receiver.drawScoreFont(engine, playerID, 3, topY+i, tableGradeNameClassic[rankingClassicGrade20g[i][type]], gcolor, scale);
								receiver.drawScoreFont(engine, playerID, 9, topY+i, String.valueOf(rankingClassicLevel20g[i][type]), (i == rankingClassicRank20g), scale);
								receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingClassicTime20g[i][type]), (i == rankingClassicRank20g), scale);
							}
							else
							{
								if((rankingGrade20g[i][type] >= 0) && (rankingGrade20g[i][type] < tableGradeName.length))
										receiver.drawScoreFont(engine, playerID, 3, topY+i, tableGradeName[rankingGrade20g[i][type]], gcolor, scale);
								receiver.drawScoreFont(engine, playerID, 9, topY+i, String.valueOf(rankingLevel20g[i][type]), (i == rankingRank20g), scale);
								receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime20g[i][type]), (i == rankingRank20g), scale);
							}
						}
						else {
							if(modetype == 1 || modetype == 3)
							{
								if((rankingClassicGrade[i][type] >= 0) && (rankingClassicGrade[i][type] < tableGradeNameClassic.length))
									receiver.drawScoreFont(engine, playerID, 3, topY+i, tableGradeNameClassic[rankingClassicGrade[i][type]], gcolor, scale);
								receiver.drawScoreFont(engine, playerID, 9, topY+i, String.valueOf(rankingClassicLevel[i][type]), (i == rankingClassicRank), scale);
								receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingClassicTime[i][type]), (i == rankingClassicRank), scale);
							}
							else
							{
								if((rankingGrade[i][type] >= 0) && (rankingGrade[i][type] < tableGradeName.length))
										receiver.drawScoreFont(engine, playerID, 3, topY+i, tableGradeName[rankingGrade[i][type]], gcolor, scale);
								receiver.drawScoreFont(engine, playerID, 9, topY+i, String.valueOf(rankingLevel[i][type]), (i == rankingRank), scale);
								receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[i][type]), (i == rankingRank), scale);
							}
						}
					}
					if(enableexam) {
						receiver.drawScoreFont(engine, playerID, 0, 14, "QUALIFIED GRADE", EventReceiver.COLOR_YELLOW);
						if (modetype == 3) receiver.drawScoreFont(engine, playerID, 0, 15, getGradeName(qualifiedClassicGrade));
						else receiver.drawScoreFont(engine, playerID, 0, 15, getGradeName(qualifiedGrade));
					}

					receiver.drawScoreFont(engine, playerID, -15, 23, "F:VIEW SECTION TIME", EventReceiver.COLOR_GREEN);
				} else {
					// Section Time
					receiver.drawScoreFont(engine, playerID, 3, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

					int totalTime = 0;
					for(int i = 0; i < SECTION_MAX; i++) {
						int type = enableexam ? 1 : 0;

						int temp = Math.min(i * 100, 2100);
						int temp2 = Math.min(((i + 1) * 100) - 1, 2100);
						if (i == SECTION_MAX -1) temp2 = 2100;

						String strSectionTime;
						strSectionTime = String.format("%3d-%3d %s", temp, temp2, GeneralUtil.getTime(bestSectionTime[i][type]));

						receiver.drawScoreFont(engine, playerID, 3, 3 + i, strSectionTime, (sectionIsNewRecord[i] && !isAnyExam()));

						totalTime += bestSectionTime[i][type];
					}

					receiver.drawScoreFont(engine, playerID, -15, 21, "TOTAL", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, -15, 22, GeneralUtil.getTime(totalTime));
					receiver.drawScoreFont(engine, playerID, -6, 21, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, -6, 22, GeneralUtil.getTime(totalTime / SECTION_MAX));

					receiver.drawScoreFont(engine, playerID, -15, 23, "F:VIEW RANKING", EventReceiver.COLOR_GREEN);
				}
			}
		} else {
			// 段位
			int rgrade = grade;
			if(modetype == 3)
			{
				if(enableexam && (rgrade >= 78) && (qualifiedClassicGrade < 72)) rgrade = 77;
				if(enableexam && (rgrade > qualifiedClassicGrade + 1) && (qualifiedClassicGrade >= 78)) rgrade = qualifiedClassicGrade + 1;
			}
			if(promotionFlag || classicPromotionFlag) {
				// и©¦йЁ“ж®µдЅЌ
				receiver.drawMenuFont(engine, playerID, 0, 21, "QUALIFY", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, 0, 22, getGradeName(classicPromotionFlag ? classicPromotionalExam : promotionalExam));
			}
			else
			{
				if(enableexam && (rgrade >= 18) && (qualifiedGrade < 18)) rgrade = 17;
			}
			receiver.drawScoreFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
			if(modetype == 1 || modetype == 3)
			{
				if((grade >= 0) && (grade < tableGradeNameClassic.length))
					receiver.drawScoreFont(engine, playerID, 0, 3, tableGradeNameClassic[grade], ((gradeflash > 0) && (gradeflash % 4 == 0)));
			}
			else {
				if((grade >= 0) && (grade < tableGradeName.length))
					receiver.drawScoreFont(engine, playerID, 0, 3, tableGradeName[grade], ((gradeflash > 0) && (gradeflash % 4 == 0)));
			}
			// Score
			receiver.drawScoreFont(engine, playerID, 0, 5, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime <= 0)) {
			strScore = String.valueOf(engine.statistics.score);
			} else if(engine.statistics.level < 2100) {
			strScore = String.valueOf(engine.statistics.score) + "\n(+" + String.valueOf(lastscore) + ")";
			}
			else strScore = String.valueOf(engine.statistics.score);
			receiver.drawScoreFont(engine, playerID, 0, 6, strScore);

			if(engine.statistics.level < 2100){
				//  level
				receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
				int tempLevel = engine.statistics.level;
				if(tempLevel < 0) tempLevel = 0;
				String strLevel = String.format("%3d", tempLevel);
				receiver.drawScoreFont(engine, playerID, 0, 10, strLevel);
				int speed = engine.speed.gravity / 128;
				if(engine.speed.gravity < 0) speed = 40;
				receiver.drawSpeedMeter(engine, playerID, 0, 11, speed);

				receiver.drawScoreFont(engine, playerID, 0, 12, String.format("%3d", nextseclv));
				
			}
			else
			{
				receiver.drawScoreFont(engine, playerID, 0, 7, "LIVES", EventReceiver.COLOR_YELLOW);
				String strLives = java.lang.Integer.toString(engine.lives);
				if(engine.lives > 0)
					strLives = String.valueOf(engine.lives + 1);
				else if (challengerGameOver)
					strLives = "0";
				else
					strLives = "1";
				receiver.drawScoreFont(engine, playerID, 0, 8, strLives);
				receiver.drawScoreFont(engine, playerID, 0, 10, "GARB DELAYS", EventReceiver.COLOR_CYAN);
				receiver.drawScoreFont(engine, playerID, 0, 11, java.lang.Integer.toString(garbageDelay));
			}

			receiver.drawScoreFont(engine, playerID, 0, 14, "GRADE SCORE", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 15, Long.toString(gradePoint)+"/");
			receiver.drawScoreFont(engine, playerID, 0, 16, Long.toString((long)gradescorereq));
			// Time
			if((engine.statistics.level == 2100 && modetype < 4) || (engine.statistics.level == 500 && modetype == 4))
				receiver.drawMenuFont(engine, playerID, 2, 20, GeneralUtil.getTime(normaltime));
			else
				receiver.drawMenuFont(engine, playerID, 2, 20, GeneralUtil.getTime(engine.statistics.time));
			receiver.drawScoreFont(engine, playerID, 0, 22, "PIECES/SEC", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 23, Double.toString(Math.floor(engine.statistics.pps * 100) / 100));

			// Roll 残り time
			if((engine.gameActive) && (engine.ending == 2)) {
				int time = ROLLTIMELIMIT - rolltime;
				if (modetype == 4) time = (int)(ROLLTIMELIMIT / 2.03) - rolltime;
				if(time < 0) time = 0;
				receiver.drawScoreFont(engine, playerID, 0, 17, "ROLL TIME", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
			}
			if(regretdispframe > 0) {
				// REGRETиЎЁз¤є
				receiver.drawMenuFont(engine,playerID,2,21,"REGRET",(regretdispframe / 2 % 2 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_ORANGE);
			} else if(tspintime > 0) {
				receiver.drawMenuFont(engine,playerID,2,21,"T-SPIN!",(tspintime / 2 % 2 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_ORANGE);
			} else if(cooldispframe > 0) {
				// COOLиЎЁз¤є
				receiver.drawMenuFont(engine,playerID,2,21,"COOL!!",(cooldispframe / 2 % 2 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_ORANGE);
			}

			//  medal
			if(medalAC >= 1) receiver.drawScoreFont(engine, playerID, 0, 19, "AC", getMedalFontColor(medalAC));
			if(medalST >= 1) receiver.drawScoreFont(engine, playerID, 3, 19, "ST", getMedalFontColor(medalST));
			if(medalSK >= 1) receiver.drawScoreFont(engine, playerID, 0, 20, "SK", getMedalFontColor(medalSK));
			if(medalRE >= 1) receiver.drawScoreFont(engine, playerID, 3, 20, "RE", getMedalFontColor(medalRE));
			if(medalRO >= 1) receiver.drawScoreFont(engine, playerID, 0, 21, "RO", getMedalFontColor(medalRO));
			if(medalCO >= 1) receiver.drawScoreFont(engine, playerID, 3, 21, "CO", getMedalFontColor(medalCO));

			// Section Time
			if((showsectiontime == true) && (sectiontime != null)) {
				int x = (receiver.getNextDisplayType() == 2) ? 22 : 12;
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				receiver.drawScoreFont(engine, playerID, x, 2, "SECTION TIME", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < sectiontime.length; i++) {
					if(sectiontime[i] > 0) {
						int temp = i * 100;
						if(temp > 2100) temp = 2100;

						int section = engine.statistics.level / 100;
						if(engine.statistics.level == 2100) section = 20;
						String strSeparator = " ";
						int color = EventReceiver.COLOR_WHITE;
						if((i == section) && (engine.ending == 0)) {
							strSeparator = "b";
						} else {
							if (regretsection[i]) color = EventReceiver.COLOR_RED;
							else if (coolsection[i] && sectionIsNewRecord[i]) color = EventReceiver.COLOR_BLUE;
							else if (sectionIsNewRecord[i]) color = EventReceiver.COLOR_DARKBLUE;
							else if (coolsection[i]) color = EventReceiver.COLOR_GREEN;
						}

						int pos = i - Math.max(section-14,0);


						String strSectionTime;
						strSectionTime = String.format("%3d%s%s", temp, strSeparator, GeneralUtil.getTime(sectiontime[i]));
						if (pos >= 0) receiver.drawScoreFont(engine, playerID, x, 3 + pos, strSectionTime, color, scale);
					}
				}

				if(sectionavgtime > 0) {
					receiver.drawScoreFont(engine, playerID, 12, 19, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 12, 20, GeneralUtil.getTime(sectionavgtime));
				}
			}
		}
	}

	/*
	 * 移動中の処理
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// 新規ピース出現時
		gradepointpps += (float) (engine.statistics.pps / (1 + ((engine.statistics.level /100)/ 4)))/60;
		if(gradepointpps >= 1.0) {
			gradePoint++;
			statGradePoints++;
			gradepointpps -= (float) 1;
		}
		if (((startlevel == 21 && modetype < 4) || (startlevel == 5 && modetype == 4)) && lvlroll == false) {lvlroll = true; engine.ending = 2;}
		if(garbageDelay > 0) garbageCount = 0;
		if((engine.statc[0] == 0) && (engine.holdDisable == false) && garbageDelay == 0) {
			// せり上がりカウント
			if((tableGarbage[speed_level / 100] != 0  && version > 2) || tableGarbage[engine.statistics.level / 100] != 0  && garbageDelay == 0) garbageCount++;
			// せり上がり
			if((((garbageCount >= tableGarbage[speed_level / 100]) && !always20g && (tableGarbage[speed_level / 100] != 0) && version > 2) || ((garbageCount >= tableGarbage[engine.statistics.level / 100]) && !always20g && (tableGarbage[engine.statistics.level / 100] != 0))) && garbageDelay == 0) {
				sendGarbage(engine);
			}
			else if((((garbageCount >= tableGarbage[speed_level / 100 +4]) && always20g && (tableGarbage[speed_level / 100 +4] != 0) && version > 2) || ((garbageCount >= tableGarbage[engine.statistics.level / 100 +4]) && always20g && (tableGarbage[engine.statistics.level / 100 +4] != 0)))&& garbageDelay == 0) {
				sendGarbage20g(engine);
			}
		}
		if((engine.statc[0] == 0)  && (engine.holdDisable == false) && garbageDelay > 0) garbageDelay--;
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!lvupflag)) {
			// Level up
			if(engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) engine.playSE("levelstop");
			}
			levelUp(engine);

			// 旧Version用
			if(version <= 1) {
				// Hard drop bonusInitialization
				harddropBonus = 0;

				// RE medal
				if((engine.timerActive == true) && (medalRE < 3)) {
					int blocks = engine.field.getHowManyBlocks();

					if(recoveryFlag == false) {
						if(blocks >= 150) {
							recoveryFlag = true;
						}
					} else {
						if(blocks <= 70) {
							recoveryFlag = false;
							engine.playSE("medal");
							medalRE++;
						}
					}
				}
			}
		}
		if( (engine.ending == 0) && (engine.statc[0] > 0) && ((version >= 1) || (engine.holdDisable == false)) ) {
			lvupflag = false;
		}

		// 段位 point減少
		if((engine.timerActive == true) && (gradePoint > 0) && (engine.combo <= 0) && (engine.lockDelayNow < engine.getLockDelay() - 1)) {
			gradeDecay++;

			int index = gradeInternal;
			if(index > tableGradeDecayRate.length - 1) index = tableGradeDecayRate.length - 1;

			if(gradeDecay >= tableGradeDecayRate[index]) {
				gradeDecay = 0;
				gradePoint--;
				statGradePoints--;
			}
		}

		if((always20g && engine.statistics.level > 1899) || furth)
		{
			owner.bgmStatus.bgm = BGMStatus.BGM_SPECIAL4;
		}
		// Endingスタート
		if((engine.ending == 2) && (rollstarted == false)) {
			rollstarted = true;

			if(mrollFlag) {
				if (version > 2)engine.blockHidden = 15;
				else engine.blockHidden = 2;
				engine.blockHiddenAnim = true;
				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;
			} else {
				engine.blockHidden = 300;
				engine.blockHiddenAnim = true;
				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;
			}

			if(always20g && modetype < 4) owner.bgmStatus.bgm = BGMStatus.BGM_SPECIAL4;
			else if (modetype == 4) owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;
			else owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
		}

		return false;
	}

	/*
	 * ARE中の処理
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		// 最後の frame
		gradepointpps += (float) (engine.statistics.pps / (1 + ((engine.statistics.level /100)/ 4)))/60;
		if(gradepointpps >= 1.0) {
			gradePoint++;
			statGradePoints++;
			gradepointpps -= (float) 1;
		}
		if((engine.ending == 0) && (engine.statc[0] >= engine.statc[1] - 1) && (!lvupflag)) {
			if(engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) engine.playSE("levelstop");
			}
			levelUp(engine);
			lvupflag = true;
		}

		return false;
	}

	/**
	 *  levelが上がったときの共通処理
	 */
	private void levelUp(GameEngine engine) {
		// Meter
		engine.meterValue = ((engine.statistics.level % 100) * receiver.getMeterMax(engine)) / 99;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.statistics.level % 100 >= 50) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.statistics.level % 100 >= 80) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.statistics.level == nextseclv - 1) engine.meterColor = GameEngine.METER_COLOR_RED;

		checkCool(engine);
		// 速度変更
		setSpeed(engine);
		if(version > 2 && furth && engine.statistics.level > 1398 || (always20g && engine.statistics.level > 998))
		{
			engine.blockHidden = 120 - engine.statistics.level / 25;
		}

		if(engine.statistics.level < 1900) owner.backgroundStatus.fadebg = engine.statistics.level / 100;
		else owner.backgroundStatus.fadebg = 19;
		// LV100到達でghost を消す
		if((engine.statistics.level >= 100) && (!alwaysghost)) engine.ghost = false;

		// BGM fadeout
		if((tableBGMFadeout[bgmlv] != -1) && (engine.statistics.level >= tableBGMFadeout[bgmlv]) && startlevel < 21 && !always20g && !furth)
			owner.bgmStatus.fadesw  = true;
		else if((tableBGMFadeout[bgmlv] != -1) && (engine.statistics.level >= tableBGMFadeout[bgmlv] -400) && startlevel < 21 && always20g && !furth)
			owner.bgmStatus.fadesw  = true;
		if(always20g && engine.statistics.level > (20 - confignpieces) * 100 && engine.statistics.level < 2000) {
			engine.ruleopt.nextDisplay = 19 - (engine.statistics.level / 100);
		}
		else if (always20g && engine.statistics.level >= 2000) engine.ruleopt.nextDisplay = 0;
		if ((engine.statistics.level / 100) > 19 && always20g) { engine.ruleopt.holdEnable = false;}
		if(version >= 2) {
			// Hard drop bonusInitialization
			harddropBonus = 0;

			// RE medal
			if((engine.timerActive == true) && (medalRE < 3)) {
				int blocks = engine.field.getHowManyBlocks();

				if(recoveryFlag == false) {
					if(blocks >= 150) {
						recoveryFlag = true;
					}
				} else {
					if(blocks <= 70) {
						recoveryFlag = false;
						engine.playSE("medal");
						medalRE++;
					}
				}
			}
		}
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Combo
		if(lines == 0) {
			comboValue = 1;
		} else {
			comboValue = comboValue + (2 * lines) - 2;
			if(comboValue < 1) comboValue = 1;
		}
		// RO medal 用カウント
		int rotateTemp = engine.nowPieceRotateCount;
		if(rotateTemp > 4) rotateTemp = 4;
		rotateCount += rotateTemp;

		if(engine.tspin)
		{
			if (lines == 1) virtualBasePoint += 10;
			if (lines == 2) virtualBasePoint += 20;
			if (lines == 3) virtualBasePoint += 30;
		}
		if(engine.statistics.level >= 600 && !always20g) engine.bone = true;
		else if(engine.statistics.level >= 200 && always20g) engine.bone = true;
		if(engine.statistics.level == 2100 || (always20g && engine.statistics.level > 1600) || (version > 2 && (speed_level == 2100 || (always20g && speed_level > 1600)))) {
			if(lines == 1) garbageDelay += 1;
			if(lines == 2) garbageDelay += 2;
			if(lines == 3) garbageDelay += 4;
			if(lines >= 4) garbageDelay += 6;
		}
		if (lines >= 1) {
			if(engine.tspin) tspintime = 60;
			// Level up
			int levelb = engine.statistics.level;
			// 段位 point
			int index = gradeInternal;
			if(index > 10) index = 10;
			int basepoint = tableGradePoint[lines - 1];
			basepoint += virtualBasePoint;
			virtualBasePoint = 0;
			
			int indexcombo = engine.combo - 1;
			if(indexcombo < 0) indexcombo = 0;
			if(indexcombo > tableGradeComboBonus[lines - 1].length - 1) indexcombo = tableGradeComboBonus[lines - 1].length - 1;
			float combobonus = tableGradeComboBonus[lines - 1][indexcombo];
	
			int levelbonus = 1 + (engine.statistics.level / 250);
	
			float point = (basepoint * combobonus) * levelbonus;
			if(furth) point *= 50;
			if(always20g && engine.statistics.level == 2100) point *= 10 + (rolltime / 300);
			else if(always20g && engine.statistics.level > 1999) point *= 5;
			else if(always20g && engine.statistics.level > 1899) point *= 2;
			gradePoint += (long)point;
			statGradePoints += (long)point;
	
			
			while(gradePoint >= gradescorereq) {
				gradePoint = (long) (gradePoint - gradescorereq);
				gradeDecay = 0;
				gradeInternal++;
				if(engine.statistics.level < 100 && (modetype == 0 || modetype == 2 || modetype == 4)) gradescorereq *= 1.25;
				else if(modetype == 0 || modetype == 2 || modetype == 4) gradescorereq =  gradescorereq * Math.abs(1 + ((Math.abs(Math.floor((double)engine.statistics.level /100)+1)/4)));
				else gradescorereq =  gradescorereq * Math.floor(1 + ((Math.floor(Math.floor((double)engine.statistics.level /100)+1)/4)));
				if(modetype == 1 || modetype == 3)
				{
					if((tableGradeChangeClassic[grade] != -1) && (gradeInternal >= tableGradeChangeClassic[grade])) {
						gradeincreaseamount++;
						gradeflash = 180;
						lastGradeTime = engine.statistics.time;
					}
				}
				else
				{
					if((tableGradeChange[grade] != -1) && (gradeInternal >= tableGradeChange[grade])) {
						gradeincreaseamount++;
						gradeflash = 180;
						lastGradeTime = engine.statistics.time;
					}
				}
			}
	
			// 4-line clearカウント
			if(lines >= 4) {
				if (engine.statistics.level < 2100)sectionfourline[engine.statistics.level / 100]++;
	
				// SK medal
				if(big == true) {
					if((engine.statistics.totalFour == 27) || (engine.statistics.totalFour == 60) || (engine.statistics.totalFour == 100) || (engine.statistics.totalFour == 200)) {
						engine.playSE("medal");
						medalSK++;
					}
				} else {
					if((engine.statistics.totalFour == 40) || (engine.statistics.totalFour == 70) || (engine.statistics.totalFour == 100) || (engine.statistics.totalFour == 200)) {
						engine.playSE("medal");
						medalSK++;
					}
				}
			}
	
			// AC medal
			if(engine.field.isEmpty() && lvlroll == true) {
				engine.playSE("bravo");
	
				if(medalAC < 3) {
					engine.playSE("medal");
					medalAC++;
				}
			}
	
			// CO medal
			if(big == true) {
				if((engine.combo >= 2) && (medalCO < 1)) {
					engine.playSE("medal");
					medalCO = 1;
				} else if((engine.combo >= 3) && (medalCO < 2)) {
					engine.playSE("medal");
					medalCO = 2;
				} else if((engine.combo >= 4) && (medalCO < 3)) {
					engine.playSE("medal");
					medalCO = 3;
				}
			} else {
				if((engine.combo >= 4) && (medalCO < 1)) {
					engine.playSE("medal");
					medalCO = 1;
				} else if((engine.combo >= 5) && (medalCO < 2)) {
					engine.playSE("medal");
					medalCO = 2;
				} else if((engine.combo >= 7) && (medalCO < 3)) {
					engine.playSE("medal");
					medalCO = 3;
				}
			}
			// Calculate score
			if(engine.ending > 0)
			{
				int manuallock = 0;
				if(engine.manualLock == true) manuallock = 1;
	
				int bravo = 1;
				if(engine.field.isEmpty()) bravo = 4;
	
				int speedBonus = engine.getLockDelay() - engine.statc[0];
				if(speedBonus < 0) speedBonus = 0;
	
				lastscore = ((levelb + lines)/4 + engine.softdropFall + manuallock + harddropBonus) * lines * comboValue * bravo +
							(engine.statistics.level / 2) + (speedBonus * 7);
				engine.statistics.score += lastscore;
			}
		}
		if((lines >= 1) && (engine.ending == 0)) {

			// Level up
			int levelb = engine.statistics.level;

			int levelplus = lines;
			if(lines == 2) levelplus = 3;
			if(lines == 3) levelplus = 6;
			if(lines >= 4) levelplus = 10;

			engine.statistics.level += levelplus;
			levelUp(engine);

			if(engine.statistics.level >= 2100 && modetype < 4) {
				// Ending
				engine.statistics.level = 2100;
				engine.timerActive = true;
				engine.ending = 1;
				rollclear = 1;

				lastGradeTime = engine.statistics.time;
				normaltime = engine.statistics.time;

				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, levelb / 100);

				// RO medal
				roMedalCheck(engine);
				
				checkRegret(engine, levelb);

				// 条件を全て満たしているなら消えRoll 発動
				mrollFlag = true;
			}
			if(engine.statistics.level >= 500 && modetype == 4) {
				// Ending
				engine.statistics.level = 500;
				engine.timerActive = true;
				engine.ending = 1;
				rollclear = 1;

				lastGradeTime = engine.statistics.time;
				normaltime = engine.statistics.time;

				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, levelb / 100);

				// RO medal
				roMedalCheck(engine);
				
				checkRegret(engine, levelb);
			} else if( ((nextseclv ==  500) && (engine.statistics.level >=  500) && (torikan > 0) && (engine.statistics.time > torikan)) ||
					   ((nextseclv == 1000) && (engine.statistics.level >= 1000) && (torikan > 0) && (engine.statistics.time > torikan * 1.5)) ||
					   ((nextseclv == 1500) && (engine.statistics.level >= 1500) && (torikan > 0) && (engine.statistics.time > torikan * 2)) ||
					   ((nextseclv == 2000) && (engine.statistics.level >= 2000) && (torikan > 0) && (engine.statistics.time > torikan * 2.3)) )
			{
				//  level500/1000гЃЁг‚Љг‚«гѓі
				engine.playSE("endingstart");

				if(nextseclv == 500) engine.statistics.level = 500;
				if(nextseclv == 1000) engine.statistics.level = 1000;

				engine.lives = 0;
				engine.gameEnded();
				engine.staffrollEnable = false;
				engine.ending = 1;

				secretGrade = engine.field.getSecretGrade();

				// Section Timeг‚’иЁ�йЊІ
				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, levelb / 100);

				if(sectionlasttime > tableTimeRegret[levelb / 100]) {
					// REGRETе€¤е®љ
					regretdispframe = 180;
					engine.playSE("regret");
				}
			} else if(engine.statistics.level >= nextseclv) {
				// Next Section
				if ((engine.statistics.level < 2100 && modetype < 4) || (engine.statistics.level < 500 && modetype == 4))
				engine.playSE("levelup");

				// Background切り替え
				if (engine.statistics.level < 2000 && modetype < 4)
				{
					owner.backgroundStatus.fadesw = true;
					owner.backgroundStatus.fadecount = 0;
					owner.backgroundStatus.fadebg = nextseclv / 100;
				}

				// BGM切り替え
				if((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= tableBGMChange[bgmlv]) && !always20g && !furth) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv;
				}

				else if((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= tableBGMChange[bgmlv] - 400) && always20g && !furth) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv;
				}

				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// 消えRoll check
				mrollCheck(levelb);

				// ST medal
				stMedalCheck(engine, levelb / 100);

				checkRegret(engine, levelb);
				
				// COOLг‚’еЏ–гЃЈгЃ¦гЃџг‚‰
				if(cool == true) {
					previouscool = true;

				} else {
					previouscool = false;
				}

				cool = false;
				coolchecked = false;
				cooldisplayed = false;
				
				// RO medal
				if((nextseclv == 300) || (nextseclv == 700)) roMedalCheck(engine);

				// Update level for next section
				nextseclv += 100;
				if(nextseclv > 2100) nextseclv = 2100;
			
			} else if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) {
				engine.playSE("levelstop");
			}
			int manuallock = 0;
			if(engine.manualLock == true) manuallock = 1;

			int bravo = 1;
			if(engine.field.isEmpty()) bravo = 4;

			int speedBonus = engine.getLockDelay() - engine.statc[0];
			if(speedBonus < 0) speedBonus = 0;

			lastscore = ((levelb + lines)/4 + engine.softdropFall + manuallock + harddropBonus) * lines * comboValue * bravo +
						(engine.statistics.level / 2) + (speedBonus * 7);
			engine.statistics.score += lastscore;

			scgettime = 120;
		} else if((lines >= 1) && (mrollFlag == true) && (engine.ending == 2)) {
			// 消えRoll 中のLine clear
			mrollLines += lines;
		}
	}

	/*
	 * Called when hard drop used
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		if(fall * 2 > harddropBonus) harddropBonus = fall * 2;
	}

	/*
	 * 各 frame の終わりの処理
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		if(gradeflash > 0) gradeflash--;
		
		if(login0arrwarningtime > 0) login0arrwarningtime--;
		if(login0arrwarningtime % 6 == 0 && login0arrwarningtime > 0 && engine.getDASDelay() == 0) engine.playSE("garbage");

		// 獲得Render score
		if(scgettime > 0) scgettime--;
		
		musicinitialised = true;
		if (confignpieces <= nextpieces20genforced || !always20g) engine.ruleopt.nextDisplay = confignpieces;
		else engine.ruleopt.nextDisplay = nextpieces20genforced;
		
		if(engine.stat == GameEngine.STAT_SETTING && musicinitialised && version > 2 && ((engine.getDASDelay() > 0 && (engine.ai == null || !AIused)) || login0arrwarningtime == 0))
		{
			if (!furth && !always20g) owner.bgmStatus.bgm = tableBGMChangeMenu[startlevel];
			else if (always20g && !furth) owner.bgmStatus.bgm = tableBGMChangeMenu20g[startlevel];
			else owner.bgmStatus.bgm = 15;
			if (modetype == 4 && startlevel == 5) owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;
		}
		else if ((engine.getDASDelay() == 0 && (engine.ai != null || AIused)) || login0arrwarningtime > 0) owner.bgmStatus.bgm = -1;
		
		if(tspintime > 0) tspintime--;
		
		if(engine.statistics.level >= 1900) {owner.backgroundStatus.bg = 19;  owner.backgroundStatus.fadebg = 19;}
		
		// REGRETиЎЁз¤є
		if(regretdispframe > 0) regretdispframe--;

		// COOLиЎЁз¤є
		if(cooldispframe > 0) cooldispframe--;

		// 15分経過
		if(engine.statistics.time >= 36000) {
			setSpeed(engine);
		}
		if(!challengerGameOver && version > 2 && ((engine.getDASDelay() > 0 && (engine.ai == null || !AIused)) || login0arrwarningtime == 0))
		{
			if(engine.statistics.level == 2100 || startlevel == 21)
			{
				if(!furth && !always20g)
				owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
				else owner.bgmStatus.bgm = BGMStatus.BGM_SPECIAL4;
			}
			if (always20g && startlevel > 18)
			{
				owner.bgmStatus.bgm = BGMStatus.BGM_SPECIAL4;
			}
		}
		if (engine.statistics.level == 2100 && modetype < 4 && rolltime < ROLLTIMELIMIT && engine.ending == 2 && !challengerGameOver) engine.statistics.time++;
		if (engine.statistics.level == 500 && modetype == 4 && rolltime < ROLLTIMELIMIT / 2.03 && engine.ending == 2 && !challengerGameOver) engine.statistics.time++;
		
		if (gradeincreaseamount > 0 && gradeincreasedelay == 0)
		{
			if ((grade < 92 || rollclear > 1) && (modetype == 1 || modetype == 3)) grade++;
			if ((grade < 18 || rollclear > 1) && (modetype == 0 || modetype == 2 || modetype == 4)) grade++;
			if ((modetype == 0 || modetype == 2 || modetype == 4) && grade == 19 && rollclear < 1) gradeincreaseamount = 0;
			if ((modetype == 1 || modetype == 3) && grade == 92 && rollclear < 1) gradeincreaseamount = 0;
			if (always20g && (modetype == 0 || modetype == 2 || modetype == 4) && grade == 19 && rollclear > 1) grade++;
			if (always20g && (modetype == 1 || modetype == 3) && grade == 92 && rollclear > 1) grade++;
			if (furth && (modetype == 0 || modetype == 2 || modetype == 4) && grade >= 19 && rollclear > 1) { grade += 2; gradeincreaseamount = 0;}
			if (furth && (modetype == 1 || modetype == 3) && grade >= 92 && rollclear > 1) { grade += 2; gradeincreaseamount = 0;}
			if (gradeincreaseamount > 0 ) gradeincreaseamount--;
			if(modetype == 1 || modetype == 3) gradeincreasedelay = 5;
			else gradeincreasedelay = 20;
			engine.playSE("gradeup");
		}
		else if(gradeincreasedelay > 0) gradeincreasedelay--;

		// Section Time増加
		if((engine.timerActive) && (engine.ending == 0)) {
			int section = engine.statistics.level / 100;

			if((section >= 0) && (section < sectiontime.length)) {
				sectiontime[section]++;
			}
		}

		// Ending
		if((engine.gameActive) && (engine.ending == 2)) {
			rolltime++;
			if(mrollFlag) {
				if (rolltime < 6400 && version > 2)engine.blockHidden = 15 - (rolltime / 400);
				engine.blockHiddenAnim = true;
				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;
			}
			

			// Time meter
			int remainRollTime = ROLLTIMELIMIT - rolltime;
			if (modetype == 4) { remainRollTime = (int)(ROLLTIMELIMIT / 2.03) - rolltime; engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / (int)(ROLLTIMELIMIT / 2.03);}
			else engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / ROLLTIMELIMIT;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(remainRollTime <= 30*60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(remainRollTime <= 20*60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(remainRollTime <= 10*60) engine.meterColor = GameEngine.METER_COLOR_RED;

			// Roll 終了
			if((rolltime >= ROLLTIMELIMIT && modetype < 4) || (rolltime >= ROLLTIMELIMIT / 2.03 && modetype == 4)) {
				rollclear = 2;
				engine.lives = 0;

				if(mrollFlag == true) {
					if (modetype == 1 || modetype == 3) gradeincreaseamount = 93 - grade;
					else gradeincreaseamount = 19 - grade;
					gradeflash = 180;
					lastGradeTime = engine.statistics.time;

					rollclear = 3;
					if(mrollLines >= 32) rollclear = 4;
				}

				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;

				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.STAT_EXCELLENT;
			}
		}
	}

	/*
	 * game over
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		// 段位M
		if(engine.lives < 1 && initgameover) { challengerGameOver = true; owner.bgmStatus.bgm = -1;}
		if((mrollFlag == true) && (grade < 92) && (engine.ending == 2) && (engine.statc[0] == 0) && engine.lives < 1) {
			if ((modetype == 1 || modetype == 3) && grade < 92) { gradeincreaseamount = 92 - grade; gradeflash = 180;}
			if ((modetype == 0 || modetype == 2) && grade < 18) { gradeincreaseamount = 18 - grade; gradeflash = 180;}
			lastGradeTime = engine.statistics.time;
		}

		if(engine.statc[0] == 0) {
			// Blockの表示を元に戻す
			engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;
			// 裏段位
			secretGrade = engine.field.getSecretGrade();

			if(enableexam && engine.lives < 1) {
				if(modetype == 3)
				{
					if(grade < (qualifiedClassicGrade - 7)) {
						classicDemotionPoints += (qualifiedClassicGrade - grade - 7);
					}
					if(classicPromotionFlag && grade >= classicPromotionalExam) {
						qualifiedClassicGrade = classicPromotionalExam;
						classicDemotionPoints = 0;
					}
					if(classicDemotionFlag && grade < classicDemotionExamGrade) {
						qualifiedClassicGrade = classicDemotionExamGrade - 1;
						if(qualifiedClassicGrade < 0) qualifiedClassicGrade = 0;
					}
				}
				else{
					if(grade < (qualifiedGrade - 7)) {
						demotionPoints += (qualifiedGrade - grade - 7);
					}
					if(promotionFlag && grade >= promotionalExam) {
						qualifiedGrade = promotionalExam;
						demotionPoints = 0;
					}
					if(demotionFlag && grade < demotionExamGrade) {
						qualifiedGrade = demotionExamGrade - 1;
						if(qualifiedGrade < 0) qualifiedGrade = 0;
					}
				}
			}
		}

		if (engine.lives < 1 && !initgameover) initgameover = true;
		return false;
	}

	/*
	 * 結果画面
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {

		if(passframe > 0) {
			if (promotionFlag) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "PROMOTION", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, 4, 6, getGradeName(promotionalExam), (passframe % 4 == 0),
						EventReceiver.COLOR_WHITE, EventReceiver.COLOR_ORANGE);

				if(passframe < 420) {
					if(grade < promotionalExam) {
						receiver.drawMenuFont(engine,playerID,3,11,"FAIL",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_RED);
					} else {
						receiver.drawMenuFont(engine,playerID,2,11,"PASS!!",(passframe % 4 == 0),EventReceiver.COLOR_ORANGE,EventReceiver.COLOR_YELLOW);
					}
				}
			} else if (demotionFlag) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "DEMOTION", EventReceiver.COLOR_RED);
				receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_RED);

				if(passframe < 420) {
					if(grade < demotionExamGrade) {
						receiver.drawMenuFont(engine,playerID,3,11,"FAIL",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_RED);
					} else {
						receiver.drawMenuFont(engine,playerID,3,11,"PASS",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_YELLOW);
					}
				}
			}
			if (classicPromotionFlag) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "PROMOTION", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, 4, 6, getGradeName(classicPromotionalExam), (passframe % 4 == 0),
						EventReceiver.COLOR_WHITE, EventReceiver.COLOR_ORANGE);

				if(passframe < 420) {
					if(grade < classicPromotionalExam) {
						receiver.drawMenuFont(engine,playerID,3,11,"FAIL",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_RED);
					} else {
						receiver.drawMenuFont(engine,playerID,2,11,"PASS!!",(passframe % 4 == 0),EventReceiver.COLOR_ORANGE,EventReceiver.COLOR_YELLOW);
					}
				}
			} else if (classicDemotionFlag) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "DEMOTION", EventReceiver.COLOR_RED);
				receiver.drawMenuFont(engine, playerID, 6, 3, "EXAM", EventReceiver.COLOR_RED);

				if(passframe < 420) {
					if(grade < classicDemotionExamGrade) {
						receiver.drawMenuFont(engine,playerID,3,11,"FAIL",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_RED);
					} else {
						receiver.drawMenuFont(engine,playerID,3,11,"PASS",(passframe % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_YELLOW);
					}
				}
			}
		} else {
			receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/4", EventReceiver.COLOR_RED);
	
			if(engine.statc[1] == 0) {
				int gcolor = EventReceiver.COLOR_WHITE;
				if((rollclear == 1) || (rollclear == 3)) gcolor = EventReceiver.COLOR_GREEN;
				if((rollclear == 2) || (rollclear == 4)) gcolor = EventReceiver.COLOR_ORANGE;
				receiver.drawMenuFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
				String strGrade;
				if (modetype == 1 || modetype == 3) strGrade = String.format("%10s", tableGradeNameClassic[grade]);
				else strGrade = String.format("%10s", tableGradeName[grade]);
				receiver.drawMenuFont(engine, playerID, 0, 3, strGrade, gcolor);
	
				if((engine.statistics.level < 2100 && modetype < 4) || (engine.statistics.level < 500 && modetype == 4)) drawResultStats(engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE,
						STAT_SCORE, STAT_LINES, STAT_LEVEL_MANIA, STAT_TIME);
				else { drawResultStats(engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE,
						STAT_SCORE, STAT_LINES, STAT_LEVEL_MANIA);
				receiver.drawMenuFont(engine, playerID, 0, 10, "TIME", EventReceiver.COLOR_BLUE);
				receiver.drawMenuFont(engine, playerID, 0, 11, String.format("%10s", GeneralUtil.getTime(normaltime)));}
				if(always20g) drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, rankingRank20g);
				else drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, rankingRank);
				if(secretGrade > 4) {
					drawResult(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE,
							"S. GRADE", String.format("%10s", tableSecretGradeName[secretGrade-1]));
				}
			} else if(engine.statc[1] == 1) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "SECT 0-10", EventReceiver.COLOR_BLUE);
	
				int color = EventReceiver.COLOR_WHITE;
				for(int i = 0; i < 11; i++) {
					if (regretsection[i]) color = EventReceiver.COLOR_RED;
					else if (coolsection[i] && sectionIsNewRecord[i]) color = EventReceiver.COLOR_BLUE;
					else if (sectionIsNewRecord[i]) color = EventReceiver.COLOR_DARKBLUE;
					else if (coolsection[i]) color = EventReceiver.COLOR_GREEN;
					else color = EventReceiver.COLOR_WHITE;
					if(sectiontime[i] > 0) {
						receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectiontime[i]), color);
					}
				}
	
				if(sectionavgtime > 0) {
					receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
				}
			}else if(engine.statc[1] == 2) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "SECT 11-20", EventReceiver.COLOR_BLUE);
	
				int color = EventReceiver.COLOR_WHITE;
				for(int i = 11; i < sectiontime.length; i++) {
					if (regretsection[i]) color = EventReceiver.COLOR_RED;
					else if (coolsection[i] && sectionIsNewRecord[i]) color = EventReceiver.COLOR_BLUE;
					else if (sectionIsNewRecord[i]) color = EventReceiver.COLOR_DARKBLUE;
					else if (coolsection[i]) color = EventReceiver.COLOR_GREEN;
					else color = EventReceiver.COLOR_WHITE;
					if(sectiontime[i] > 0) {
						receiver.drawMenuFont(engine, playerID, 2, 3 + i - 11, GeneralUtil.getTime(sectiontime[i]), color);
					}
				}
	
				if(sectionavgtime > 0) {
					receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
				}
			} else if(engine.statc[1] == 3) {
				receiver.drawMenuFont(engine, playerID, 0, 2, "MEDAL", EventReceiver.COLOR_BLUE);
				if(medalAC >= 1) receiver.drawMenuFont(engine, playerID, 5, 3, "AC", getMedalFontColor(medalAC));
				if(medalST >= 1) receiver.drawMenuFont(engine, playerID, 8, 3, "ST", getMedalFontColor(medalST));
				if(medalSK >= 1) receiver.drawMenuFont(engine, playerID, 5, 4, "SK", getMedalFontColor(medalSK));
				if(medalRE >= 1) receiver.drawMenuFont(engine, playerID, 8, 4, "RE", getMedalFontColor(medalRE));
				if(medalRO >= 1) receiver.drawMenuFont(engine, playerID, 5, 5, "RO", getMedalFontColor(medalRO));
				if(medalCO >= 1) receiver.drawMenuFont(engine, playerID, 8, 5, "CO", getMedalFontColor(medalCO));
	
				drawResultStats(engine, playerID, receiver, 6, EventReceiver.COLOR_BLUE,
						STAT_LPM, STAT_SPM, STAT_PIECE, STAT_PPS);
				drawResult(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE, "TOTAL GRSC", String.format("%10s", statGradePoints));
			}
		}
	}

	/*
	 * 結果画面の処理
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		if(passframe > 0) {
			engine.allowTextRenderByReceiver = false; // Turn off RETRY/END menu

			if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_B)) {
				if(passframe > 420)
					passframe = 420;
				else if(passframe < 300)
					passframe = 0;
			}

			if(modetype == 3)
			{
				if(classicPromotionFlag) {
					if(passframe == 420) {
						if(grade >= classicPromotionalExam) {
							engine.playSE("excellent");
						} else {
							engine.playSE("regret");
						}
					}
				} else if(classicDemotionFlag) {
					if(passframe == 420) {
						if(grade >= qualifiedClassicGrade) {
							engine.playSE("gradeup");
						} else {
							engine.playSE("gameover");
						}
					}
				}
			}
			else {
				if(promotionFlag) {
					if(passframe == 420) {
						if(grade >= promotionalExam) {
							engine.playSE("excellent");
						} else {
							engine.playSE("regret");
						}
					}
				} else if(demotionFlag) {
					if(passframe == 420) {
						if(grade >= qualifiedGrade) {
							engine.playSE("gradeup");
						} else {
							engine.playSE("gameover");
						}
					}
				}
			}

			passframe--;
			return true;
		}

		engine.allowTextRenderByReceiver = true;

		// гѓљгѓјг‚ёе€‡г‚Љж›їгЃ€
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
			engine.statc[1]--;
			if(engine.statc[1] < 0) engine.statc[1] = 3;
			engine.playSE("change");
		}
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
			engine.statc[1]++;
			if(engine.statc[1] > 3) engine.statc[1] = 0;
			engine.playSE("change");
		}
		//  section time displayе€‡ж›ї
		if(engine.ctrl.isPush(Controller.BUTTON_F)) {
			engine.playSE("change");
			isShowBestSectionTime = !isShowBestSectionTime;
		}

		engine.statc[2]++;

		return false;
	}

	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(owner.replayProp, engine.ruleopt.strRuleName);
		if(modetype == 1 || modetype == 3) owner.replayProp.setProperty("result.grade.name", tableGradeNameClassic[grade]);
		else owner.replayProp.setProperty("result.grade.name", tableGradeName[grade]);
		owner.replayProp.setProperty("result.grade.number", grade);
		owner.replayProp.setProperty("challenger.version", version);
		owner.replayProp.setProperty("challenger.furthest", furth);
		if(modetype == 3)
		{
			owner.replayProp.setProperty("challenger.classicexam", (classicPromotionFlag ? classicPromotionalExam : 0));
			owner.replayProp.setProperty("challenger.classicdemopoint", classicDemotionPoints);
			owner.replayProp.setProperty("challenger.classicdemotionExamGrade", classicDemotionExamGrade);
		}
		else
		{
			owner.replayProp.setProperty("challenger.exam", (promotionFlag ? promotionalExam : 0));
			owner.replayProp.setProperty("challenger.demopoint", demotionPoints);
			owner.replayProp.setProperty("challenger.demotionExamGrade", demotionExamGrade);
		}
		owner.replayProp.setProperty("challenger.aitype", AIused);

		// Update rankings
		if((owner.replayMode == false) && (startlevel == 0) && (big == false) && (engine.ai == null) && dtetlives == 1 && modetype < 4 && !furth) {

			int rgrade = grade;
			if (modetype == 3)
			{
				if(enableexam && (rgrade >= 92) && (qualifiedClassicGrade < 92)) {
					rgrade = 92;
				}
			}
			else
			{
				if(enableexam && (rgrade >= 18) && (qualifiedGrade < 18)) {
					rgrade = 18;
				}
			}
			if(!enableexam || !isAnyExam()) {
				updateRanking(rgrade, engine.statistics.level, lastGradeTime, rollclear, enableexam ? 1 : 0);
			} else {
				rankingRank = -1;
			}

			if(enableexam) updateGradeHistory(grade);

			if((medalST == 3) && !isAnyExam()) updateBestSectionTime();

			if((rankingRank != -1) || (enableexam) || (medalST == 3)) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < RANKING_TYPE; j++) {
				if(j == 0) {
					rankingClassicGrade[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".grade." + i, 0);
					rankingClassicLevel[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".level." + i, 0);
					rankingClassicTime[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".time." + i, 0);
					rankingClassicRollclear[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".rollclear." + i, 0);
					rankingClassicGrade20g[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".20g.grade." + i, 0);
					rankingClassicLevel20g[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".20g.level." + i, 0);
					rankingClassicTime20g[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".20g.time." + i, 0);
					rankingClassicRollclear20g[i][j] = prop.getProperty("challenger.ranking.classic." + ruleName + ".20g.rollclear." + i, 0);
					rankingGrade[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".grade." + i, 0);
					rankingLevel[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".level." + i, 0);
					rankingTime[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".time." + i, 0);
					rankingRollclear[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".rollclear." + i, 0);
					rankingGrade20g[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".20g.grade." + i, 0);
					rankingLevel20g[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".20g.level." + i, 0);
					rankingTime20g[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".20g.time." + i, 0);
					rankingRollclear20g[i][j] = prop.getProperty("challenger.ranking." + ruleName + ".20g.rollclear." + i, 0);
				} else {
					rankingClassicGrade[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".grade." + i, 0);
					rankingClassicLevel[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".level." + i, 0);
					rankingClassicTime[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".time." + i, 0);
					rankingClassicRollclear[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".rollclear." + i, 0);
					rankingClassicGrade20g[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".20g.grade." + i, 0);
					rankingClassicLevel20g[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".20g.level." + i, 0);
					rankingClassicTime20g[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".20g.time." + i, 0);
					rankingClassicRollclear20g[i][j] = prop.getProperty("challenger.ranking.classic.exam." + ruleName + ".20g.rollclear." + i, 0);
					rankingGrade[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".grade." + i, 0);
					rankingLevel[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".level." + i, 0);
					rankingTime[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".time." + i, 0);
					rankingRollclear[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".rollclear." + i, 0);
					rankingGrade20g[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".20g.grade." + i, 0);
					rankingLevel20g[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".20g.level." + i, 0);
					rankingTime20g[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".20g.time." + i, 0);
					rankingRollclear20g[i][j] = prop.getProperty("challenger.ranking.exam." + ruleName + ".20g.rollclear." + i, 0);
				}
			}
		}
		for(int i = 0; i < SECTION_MAX; i++) {
			for(int j = 0; j < RANKING_TYPE; j++) {
				bestSectionTime[i][j] = prop.getProperty("challenger.bestSectionTime."+ j+ "." + ruleName + "." + i, DEFAULT_SECTION_TIME);
			}
		}
		for(int i = 0; i < GRADE_HISTORY_SIZE; i++) {
			gradeHistory[i] = prop.getProperty("challenger.gradehistory." + ruleName + "." + i, -1);
		}
		for(int i = 0; i < CLASSIC_GRADE_HISTORY_SIZE; i++) {
			classicGradeHistory[i] = prop.getProperty("challenger.classicgradehistory." + ruleName + "." + i, -1);
		}
		qualifiedGrade = prop.getProperty("challenger.qualified." + ruleName, 0);
		qualifiedClassicGrade = prop.getProperty("challenger.qualified.classic." + ruleName, 0);
		demotionPoints = prop.getProperty("challenger.demopoint." + ruleName, 0);
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < RANKING_TYPE; j++) {
				if(always20g && !furth) {
					if(j == 0) {
						if (modetype == 1)
						{
							prop.setProperty("challenger.ranking.classic." + ruleName + ".20g.grade." + i, rankingClassicGrade20g[i][j]);
							prop.setProperty("challenger.ranking.classic." + ruleName + ".20g.level." + i, rankingClassicLevel20g[i][j]);
							prop.setProperty("challenger.ranking.classic." + ruleName + ".20g.time." + i, rankingClassicTime20g[i][j]);
							prop.setProperty("challenger.ranking.classic." + ruleName + ".20g.rollclear." + i, rankingClassicRollclear20g[i][j]);
						}
						else
						{
							prop.setProperty("challenger.ranking." + ruleName + ".20g.grade." + i, rankingGrade20g[i][j]);
							prop.setProperty("challenger.ranking." + ruleName + ".20g.level." + i, rankingLevel20g[i][j]);
							prop.setProperty("challenger.ranking." + ruleName + ".20g.time." + i, rankingTime20g[i][j]);
							prop.setProperty("challenger.ranking." + ruleName + ".20g.rollclear." + i, rankingRollclear20g[i][j]);
						}
					} 
					else
					{
						if (modetype == 3)
						{
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".20g.grade." + i, rankingClassicGrade20g[i][j]);
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".20g.level." + i, rankingClassicLevel20g[i][j]);
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".20g.time." + i, rankingClassicTime20g[i][j]);
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".20g.rollclear." + i, rankingClassicRollclear20g[i][j]);
						}
						else
						{
							prop.setProperty("challenger.ranking.exam." + ruleName + ".20g.grade." + i, rankingGrade20g[i][j]);
							prop.setProperty("challenger.ranking.exam." + ruleName + ".20g.level." + i, rankingLevel20g[i][j]);
							prop.setProperty("challenger.ranking.exam." + ruleName + ".20g.time." + i, rankingTime20g[i][j]);
							prop.setProperty("challenger.ranking.exam." + ruleName + ".20g.rollclear." + i, rankingRollclear20g[i][j]);
						}
					}
				}
				else if (!furth) {
					if(j == 0) {
						if (modetype == 1)
						{
							prop.setProperty("challenger.ranking.classic." + ruleName + ".grade." + i, rankingClassicGrade[i][j]);
							prop.setProperty("challenger.ranking.classic." + ruleName + ".level." + i, rankingClassicLevel[i][j]);
							prop.setProperty("challenger.ranking.classic." + ruleName + ".time." + i, rankingClassicTime[i][j]);
							prop.setProperty("challenger.ranking.classic." + ruleName + ".rollclear." + i, rankingClassicRollclear[i][j]);
						}
						else
						{
							prop.setProperty("challenger.ranking." + ruleName + ".grade." + i, rankingGrade[i][j]);
							prop.setProperty("challenger.ranking." + ruleName + ".level." + i, rankingLevel[i][j]);
							prop.setProperty("challenger.ranking." + ruleName + ".time." + i, rankingTime[i][j]);
							prop.setProperty("challenger.ranking." + ruleName + ".rollclear." + i, rankingRollclear[i][j]);
						}
					}
					else {
						if (modetype == 3)
						{
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".grade." + i, rankingClassicGrade[i][j]);
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".level." + i, rankingClassicLevel[i][j]);
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".time." + i, rankingClassicTime[i][j]);
							prop.setProperty("challenger.ranking.classic.exam." + ruleName + ".rollclear." + i, rankingClassicRollclear[i][j]);
						}
						else
						{
							prop.setProperty("challenger.ranking.exam." + ruleName + ".grade." + i, rankingGrade[i][j]);
							prop.setProperty("challenger.ranking.exam." + ruleName + ".level." + i, rankingLevel[i][j]);
							prop.setProperty("challenger.ranking.exam." + ruleName + ".time." + i, rankingTime[i][j]);
							prop.setProperty("challenger.ranking.exam." + ruleName + ".rollclear." + i, rankingRollclear[i][j]);
						}
					}
				}
			}
		}
		if (!furth)
		{
			for(int i = 0; i < SECTION_MAX; i++) {
				for(int j = 0; j < RANKING_TYPE; j++) {
					prop.setProperty("challenger.bestSectionTime." + j + "." + ruleName + "." + i, bestSectionTime[i][j]);
				}
			}

			if (modetype == 3)
			for(int i = 0; i < CLASSIC_GRADE_HISTORY_SIZE; i++) {
				prop.setProperty("challenger.classicgradehistory." + ruleName+ "." + i, classicGradeHistory[i]);
			}
			else
				for(int i = 0; i < GRADE_HISTORY_SIZE; i++) {
					prop.setProperty("challenger.gradehistory." + ruleName+ "." + i, gradeHistory[i]);
				}
			prop.setProperty("challenger.qualified." + ruleName, qualifiedGrade);
			prop.setProperty("challenger.qualified.classic." + ruleName, qualifiedClassicGrade);
			prop.setProperty("challenger.demopoint." + ruleName, demotionPoints);
		}
	}

	/**
	 * Update rankings
	 * @param gr 段位
	 * @param lv  level
	 * @param time Time
	 */
	private void updateRanking(int gr, int lv, int time, int clear, int type) {
		rankingRank = checkRanking(gr, lv, time, clear, type);
		rankingRank20g = checkRanking(gr, lv, time, clear, type);
		rankingClassicRank = checkRanking(gr, lv, time, clear, type);
		rankingClassicRank20g = checkRanking(gr, lv, time, clear, type);

		if(always20g) {
			if(modetype == 1 || modetype == 3)
			{
				if(rankingClassicRank20g != -1) {
					// Shift down ranking entries
					for(int i = RANKING_MAX - 1; i > rankingClassicRank20g; i--) {
							rankingClassicGrade20g[i][type] = rankingClassicGrade20g[i - 1][type];
							rankingClassicLevel20g[i][type] = rankingClassicLevel20g[i - 1][type];
							rankingClassicTime20g[i][type] = rankingClassicTime20g[i - 1][type];
							rankingClassicRollclear20g[i][type] = rankingClassicRollclear20g[i - 1][type];
					}
	
					// Add new data
					rankingClassicGrade20g[rankingClassicRank20g][type] = gr;
					rankingClassicLevel20g[rankingClassicRank20g][type] = lv;
					rankingClassicTime20g[rankingClassicRank20g][type] = time;
					rankingClassicRollclear20g[rankingClassicRank20g][type] = clear;
				}
			}
			else
			{
				if(rankingRank20g != -1) {
					// Shift down ranking entries
					for(int i = RANKING_MAX - 1; i > rankingRank20g; i--) {
							rankingGrade20g[i][type] = rankingGrade20g[i - 1][type];
							rankingLevel20g[i][type] = rankingLevel20g[i - 1][type];
							rankingTime20g[i][type] = rankingTime20g[i - 1][type];
							rankingRollclear20g[i][type] = rankingRollclear20g[i - 1][type];
					}
	
					// Add new data
					rankingGrade20g[rankingRank20g][type] = gr;
					rankingLevel20g[rankingRank20g][type] = lv;
					rankingTime20g[rankingRank20g][type] = time;
					rankingRollclear20g[rankingRank20g][type] = clear;
				}
			}
		}
		else {
			if(modetype == 1 || modetype == 3)
			{
				if(rankingClassicRank != -1) {
					// Shift down ranking entries
					for(int i = RANKING_MAX - 1; i > rankingClassicRank; i--) {
						rankingClassicGrade[i][type] = rankingClassicGrade[i - 1][type];
						rankingClassicLevel[i][type] = rankingClassicLevel[i - 1][type];
						rankingClassicTime[i][type] = rankingClassicTime[i - 1][type];
						rankingClassicRollclear[i][type] = rankingClassicRollclear[i - 1][type];
					}

					// Add new data
					rankingClassicGrade[rankingClassicRank][type] = gr;
					rankingClassicLevel[rankingClassicRank][type] = lv;
					rankingClassicTime[rankingClassicRank][type] = time;
					rankingClassicRollclear[rankingClassicRank][type] = clear;
				}
			}
			else
			{
				if(rankingRank != -1) {
					// Shift down ranking entries
					for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
						rankingGrade[i][type] = rankingGrade[i - 1][type];
						rankingLevel[i][type] = rankingLevel[i - 1][type];
						rankingTime[i][type] = rankingTime[i - 1][type];
						rankingRollclear[i][type] = rankingRollclear[i - 1][type];
					}
	
					// Add new data
					rankingGrade[rankingRank][type] = gr;
					rankingLevel[rankingRank][type] = lv;
					rankingTime[rankingRank][type] = time;
					rankingRollclear[rankingRank][type] = clear;
				}
			}
		}
	}

	/**
	 * Calculate ranking position
	 * @param gr 段位
	 * @param lv  level
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int gr, int lv, int time, int clear, int type) {
		if(always20g) {
			if (modetype == 1 || modetype == 3)
			{
				for(int i = 0; i < RANKING_MAX; i++) {
					if(gr > rankingClassicGrade20g[i][type]) {
						return i;
					} else if((gr == rankingClassicGrade20g[i][type]) && (clear > rankingClassicRollclear20g[i][type])) {
						return i;
					} else if((gr == rankingClassicGrade20g[i][type]) && (clear == rankingClassicRollclear20g[i][type]) && (lv > rankingClassicLevel20g[i][type])) {
						return i;
					} else if((gr == rankingClassicGrade20g[i][type]) && (clear == rankingClassicRollclear20g[i][type]) && (lv == rankingClassicLevel20g[i][type]) &&
							  (time < rankingClassicTime20g[i][type]))	return i;
				}
				
			}
			for(int i = 0; i < RANKING_MAX; i++) {
				if(gr > rankingGrade20g[i][type]) {
					return i;
				} else if((gr == rankingGrade20g[i][type]) && (clear > rankingRollclear20g[i][type])) {
					return i;
				} else if((gr == rankingGrade20g[i][type]) && (clear == rankingRollclear20g[i][type]) && (lv > rankingLevel20g[i][type])) {
					return i;
				} else if((gr == rankingGrade20g[i][type]) && (clear == rankingRollclear20g[i][type]) && (lv == rankingLevel20g[i][type]) &&
						  (time < rankingTime20g[i][type]))	return i;
			}
		}
		else {
			if (modetype == 1 || modetype == 3)
			{
				for(int i = 0; i < RANKING_MAX; i++) {
					if(gr > rankingClassicGrade[i][type]) {
						return i;
					} else if((gr == rankingClassicGrade[i][type]) && (clear > rankingClassicRollclear[i][type])) {
						return i;
					} else if((gr == rankingClassicGrade[i][type]) && (clear == rankingClassicRollclear[i][type]) && (lv > rankingClassicLevel[i][type])) {
						return i;
					} else if((gr == rankingClassicGrade[i][type]) && (clear == rankingClassicRollclear[i][type]) && (lv == rankingClassicLevel[i][type]) &&
							  (time < rankingClassicTime[i][type]))	return i;
				}
				
			}
			else {
				for(int i = 0; i < RANKING_MAX; i++) {
					if(gr > rankingGrade[i][type]) {
						return i;
					} else if((gr == rankingGrade[i][type]) && (clear > rankingRollclear[i][type])) {
						return i;
					} else if((gr == rankingGrade[i][type]) && (clear == rankingRollclear[i][type]) && (lv > rankingLevel[i][type])) {
						return i;
					} else if((gr == rankingGrade[i][type]) && (clear == rankingRollclear[i][type]) && (lv == rankingLevel[i][type]) &&
							  (time < rankingTime[i][type]))	return i;
				}
			}
		}

		return -1;
	}

	/**
	 * ж®µдЅЌе±Ґж­ґг‚’ж›ґж–°
	 * @param gr ж®µдЅЌ
	 */
	private void updateGradeHistory(int gr) {
		if (modetype == 3) {
			for(int i = CLASSIC_GRADE_HISTORY_SIZE - 1; i > 0; i--) {
				classicGradeHistory[i] = classicGradeHistory[i - 1];
			}
			classicGradeHistory[0] = gr;
		}
		else {
			for(int i = GRADE_HISTORY_SIZE - 1; i > 0; i--) {
				gradeHistory[i] = gradeHistory[i - 1];
			}
			gradeHistory[0] = gr;
		}
	}

	/**
	 * ж�‡ж ји©¦йЁ“гЃ®з›®жЁ™ж®µдЅЌг‚’иЁ­е®љ
	 * @author Zircean
	 */
	private void setPromotionalGrade() {
		int gradesOver;
		int classicGradesOver;

		if(modetype == 3)
		{
			for(int i = tableGradeNameClassic.length - 1; i >= 0; i--) {
				classicGradesOver = 0;
				for(int j = 0; j < CLASSIC_GRADE_HISTORY_SIZE; j++) {
					if(classicGradeHistory[j] == -1) {
						classicPromotionalExam = 0;
						return;
					} else {
						if(classicGradeHistory[j] >= i) {
							classicGradesOver++;
						}
					}
				}
				if(classicGradesOver > 3) {
					classicPromotionalExam = i;
					if(qualifiedClassicGrade < 92 && classicPromotionalExam == 93) {
						classicPromotionalExam = 92;
					}
					return;
				}
			}
		}
		else {
			for(int i = tableGradeName.length - 1; i >= 0; i--) {
				gradesOver = 0;
				for(int j = 0; j < GRADE_HISTORY_SIZE; j++) {
					if(gradeHistory[j] == -1) {
						promotionalExam = 0;
						return;
					} else {
						if(gradeHistory[j] >= i) {
							gradesOver++;
						}
					}
				}
				if(gradesOver > 3) {
					promotionalExam = i;
					if(qualifiedGrade < 18 && promotionalExam == 19) {
						promotionalExam = 18;
					}
					return;
				}
			}
		}
	}
	/**
	 * Update best section time records
	 */
	private void updateBestSectionTime() {
		for(int i = 0; i < SECTION_MAX; i++) {
			if(sectionIsNewRecord[i]) {
				int type = enableexam ? 1 : 0;
				bestSectionTime[i][type] = sectiontime[i];
			}
		}
	}
}
