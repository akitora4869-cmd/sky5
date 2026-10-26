# 敵のRPGステータス対応（Forge 1.20.1）

- Monster系の敵（ゾンビ、スケルトン等）にSkyEnemyLevelを保存し、初回出現時に近くのプレイヤーのレベルを基準に設定。周囲にプレイヤーがいない場合Lv1。
- 敵もHP / EP / STR / DEF / ATS / ADF / SPD / DEX / AGL / MOV / RNGを共通計算。Lv1敵HP140、Lv50敵HP9301。
- 敵の通常近接攻撃は敵STRと被害者DEFに基づいて計算。プレイヤー・仲間の物理攻撃も敵DEFを参照。
- 既存の `/sky test enemy <level>` で指定Lvのゾンビを召喚。`/sky enemy-stats` で近くの敵のステータスを表示。
- 既存のパーティHUD、エステルNPC、棒武器は維持。敵HUDは今回追加しない。
- 注意：既存のバニラ敵のAI、装備、特殊攻撃、遠距離攻撃、アーツ、SPD/DEX/AGLの戦闘への完全反映は未対応。敵のEPは保持されるが消費するAIは未実装。
- Gradleビルドと実機テストは未検証。
