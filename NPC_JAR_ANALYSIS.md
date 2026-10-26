# 鬼滅の刃Mod JAR解析とSkyCombatへの反映（v0.1.1）

対象: KimetsunoYaiba-ver3-forge-1.20.1.jar（ユーザー提供）。`javap -p -c`によるバイトコード構造確認。第三者のクラスファイル、テクスチャ、モデル、ソースを同梱していません。

## 確認した根拠
- `net.mcreator.kimetsunoyaiba.entity.TanjiroEntity` は `TamableAnimal` を継承し、`GeoEntity` を実装。
- `registerGoals` に相当する `m_8099_` で `FollowOwnerGoal`（優先度1、速度1.0、開始距離24、停止距離16）、近接攻撃用の匿名Goal、`HurtByTargetGoal`、`OwnerHurtByTargetGoal`、`OwnerHurtTargetGoal` を登録。
- `mobInteract` に相当する `m_6071_` は右クリック時の処理を持ち、スポーンエッグ分岐やテイム状態判定がある。会話GUIがあるとまではこの解析では断定できない。
- `setTexture`、`setAnimation`、`registerControllers` があり、GeckoLibのモデル描画・アニメーション切替を採用。
- キャラクター別Renderer、Model、右クリックProcedure、自然スポーン条件Procedureが収録されている。

## SkyCombat v0.1.1で実際に変更した部分
- `Companion.registerGoals()` に `HurtByTargetGoal` を追加し、NPC自身が攻撃を受けた敵への反撃を補強。
- 仲間NPCはオーナーを攻撃した敵だけでなく、オーナーが攻撃した敵モンスターもターゲットにする。
- 技の発動前にターゲットの生存を確認する。

## 現状の制限・次の改修
- 現行 `Companion extends Zombie` は試験実装で、プレイヤー型の見た目や完全な友好AIを保証しない。バニラZombie固有のターゲット・日光・音声・行動を引き継ぐため、本格版では `TamableAnimal` または `PathfinderMob` ベースの独自Entityに置き換える。
- `TamableAnimal` にすると所有者への追従・被攻撃援護のGoalを活用できるが、複数プレイヤー共有パーティや固有NPC一意性は独自制御が必要。
- 右クリック会話GUI、GeckoLibモデル、クォーツに基づくアーツ選択、AIのスコアリング、装備描画、NPC一意性の全チャンク検証は未実装。
- Forge開発環境でのコンパイル・ゲーム内動作テストは未実施。ビルド成功を保証するものではない。
