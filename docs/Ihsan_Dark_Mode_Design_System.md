# إحسان — Dark Mode Design System

## 1) الاتجاه البصري
الهوية المقترحة للدارك مود هي **Dark Emerald / Charcoal**:
- خلفيات فحمية مائلة للأخضر، وليست سوداء خالصة.
- طبقات Surface واضحة ومتدرجة.
- أخضر زمردي أساسي.
- Mint Accent محدود.
- Gold Accent للعناصر الإسلامية والزخارف فقط.
- الحفاظ على نفس Geometry الموجودة في Light Mode؛ الاختلاف في الألوان فقط.

---

## 2) Color Palette

### Core Colors
| Token | Hex | الاستخدام |
|---|---|---|
| `background` | `#07110E` | خلفية الشاشة الأساسية |
| `surface` | `#0E1C17` | سطح عام / Card ثانوية |
| `surfaceContainer` | `#14271F` | Card رئيسية / Elevated surface |
| `surfaceVariant` | `#1A3028` | Inputs / Secondary containers |
| `primary` | `#0D7564` | الأزرار الأساسية والعناصر الرئيسية |
| `primaryContainer` | `#174D43` | Selected state / Active container |
| `accentMint` | `#33C6AE` | Active icon / Progress / Switch accents |
| `brandGold` | `#C7A35C` | الزخارف الإسلامية / تفاصيل الشعار / القرآن |
| `outline` | `#294239` | Borders / Dividers |
| `warning` | `#D89A45` | Warning state |
| `error` | `#D9676E` | Error state |

### Text Colors
| Token | Hex | الاستخدام |
|---|---|---|
| `textPrimary` | `#F2F5F2` | العناوين والنصوص الأساسية |
| `textBody` | `#D7E0DC` | النص العادي |
| `textSecondary` | `#A7B8B1` | النصوص الثانوية والوصف |
| `textDisabled` | `#6F817A` | Disabled / low emphasis |

### Bottom Navigation
| Token | Hex |
|---|---|
| `bottomNavBackground` | `#0D1916` |
| `bottomNavSelected` | `#33C6AE` |
| `bottomNavUnselected` | `#879992` |

---

## 3) Surface Hierarchy

يجب أن يظهر الفرق بين طبقات الشاشة بوضوح:

```text
Screen Background      #07110E
Main Surface           #0E1C17
Elevated Card          #14271F
Input / Sub Surface    #1A3028
Selected Container     #174D43
```

### القاعدة
لا تعتمد على Borders فقط لفصل العناصر.  
الفصل الأساسي يجب أن يأتي من اختلاف الـSurface نفسه.

---

## 4) Hero / Header

- استخدم نفس صورة السماء والمآذن الموجودة في Light Mode.
- لا تجعل الصورة مظلمة جدًا.
- استخدم Scrim / Gradient فقط للحفاظ على وضوح النص.

### Gradient مقترح
```text
Top: transparent أو overlay خفيف
Bottom: #07110E بعتامة 65%–85%
```

### الهدف
- السماء والمآذن تبقى ظاهرة.
- النصوص تبقى قابلة للقراءة.
- الـHeader لا يتحول إلى كتلة سوداء أو خضراء غامقة.

---

## 5) Cards

### Main Card
```text
Background: #14271F
Text: #F2F5F2
Secondary Text: #A7B8B1
Border: #294239 (subtle)
```

### Secondary Card
```text
Background: #0E1C17
```

### Sub Area / Inner Container
```text
Background: #1A3028
```

### قواعد
- Rounded corners متوسطة وناعمة.
- Shadows خفيفة جدًا.
- لا تستخدم Stroke تركوازي قوي حول كل Card.

---

## 6) Inputs

```text
Background       #1A3028
Normal Border    #294239
Focused Border   #0D7564
Text             #D7E0DC
Hint             #A7B8B1
Cursor/Focus     #33C6AE
```

### القاعدة
لا تجعل TextField سوداء مع Border Neon.

---

## 7) Buttons

### Primary Button
```text
Background  #0D7564
Text        #F2F5F2
```

### Secondary Button
```text
Background  #174D43
Text        #D7E0DC
```

### Outline / Ghost
```text
Border      #294239
Text        #D7E0DC
Background  transparent
```

### القاعدة
`#33C6AE` لا يستخدم كخلفية كبيرة إلا عند وجود سبب بصري واضح.

---

## 8) Chips / Filters

### Unselected
```text
Background  #1A3028
Text        #D7E0DC
```

### Selected
```text
Background  #174D43
Text        #F2F5F2
```

### Active Indicator
```text
#33C6AE
```

---

## 9) Icons

### Default
```text
#D7E0DC
```

### Secondary
```text
#A7B8B1
```

### Active
```text
#33C6AE
```

### Islamic / Decorative
```text
#C7A35C
```

---

## 10) Progress Indicators

### Active progress
```text
#33C6AE
```

### Primary progress
```text
#0D7564
```

### Inactive track
```text
#294239
```

يمكن استخدام درجات Blue-Green بسيطة لبعض المؤشرات كما في Light Mode بشرط ألا تتحول الواجهة إلى ألوان نيون.

---

## 11) Community / Ihsan Cards

- الخلفية: `#14271F`
- الصور تحتفظ بألوانها الطبيعية.
- لا تطبق Dark Filter عالميًا على الصور.
- Buttons صغيرة ومتكاملة مع الـCard.
- Badges تستخدم `primaryContainer` أو Status colors.
- Text hierarchy:
  - Title: `#F2F5F2`
  - Body: `#D7E0DC`
  - Metadata: `#A7B8B1`

---

## 12) Live Card

- الصورة تبقى واضحة وطبيعية.
- يمكن استخدام Gradient أسود/أخضر خفيف فقط أسفل النص.
- Live badge:
  - خلفية حمراء muted.
  - لا تستخدم أحمر Neon.
- Play button:
  - White / Off-white icon.
  - Surface شفاف/داكن.

---

## 13) Quran Card على الرئيسية

يمكن أن تكون أغمق من النسخة الفاتحة لكن يجب الحفاظ على الهوية الروحانية:

```text
Card Background    #0E241E أو #14271F
Text Primary       #F2F5F2
Gold Quran Accent  #C7A35C
Button             #0D7564
```

الصورة القرآنية لا تُظلم بالكامل.

---

## 14) Quran Reader — استثناء مهم

**صفحة المصحف نفسها لا تتحول إلى Dark Mode.**

### Quran Page Colors
```text
Paper              #F8F3E7
Quran Text         #123A32
Decorative Green   #537D70
Decorative Gold    #B99350
```

### ما يصبح Dark
- App background
- Top bar
- Surrounding chrome
- Audio player

### ما يبقى Light/Warm
- صفحة المصحف نفسها
- الزخارف
- النص القرآني

السبب: راحة القراءة + الحفاظ على هوية المصحف الحقيقية.

---

## 15) Quran Audio Player

```text
Background          #14271F
Primary Controls    #F2F5F2
Play Button         #0D7564
Progress Active     #33C6AE
Progress Inactive   #294239
Secondary Text      #A7B8B1
```

---

## 16) Names of Allah

استخدم Surface دافئة مختلفة قليلًا عن بقية الـDark Theme:

```text
Warm Dark Surface  #241F16 أو #292319
Gold               #C7A35C
Primary Text       #F2F5F2
Secondary Text     #A7B8B1
```

لا تجعل الشاشة كلها أخضر غامق.

---

## 17) Settings / Search / Reminders

```text
Screen Background   #07110E
Section Surface      #0E1C17
Row Surface          #14271F
Icon Container       #1A3028
Enabled Switch       #0D7564
Switch Accent        #33C6AE
Divider              #294239
```

---

## 18) Bottom Navigation

```text
Background      #0D1916
Selected        #33C6AE
Unselected      #879992
Text Active     #F2F5F2
```

### القواعد
- لا Glow.
- لا Giant pill.
- Selected state واضح وبسيط.
- Bottom bar أفتح قليلًا من background حتى يظهر كطبقة مستقلة.

---

## 19) Splash / Onboarding

- لا تجعل Splash سوداء.
- حافظ على:
  - السماء
  - المآذن
  - الشعار
  - الـillustration
- يمكن خفض brightness قليلًا فقط.
- حافظ على نفس الهوية البصرية للـLight Mode.

---

## 20) Light/Dark Geometry

يجب أن تبقى العناصر نفسها بين الوضعين:

- Font sizes
- Spacing
- Padding
- Radius
- Card height
- Image size
- Header height
- Bottom navigation height
- Scroll behavior

**Dark Mode = Color adaptation فقط، وليس Redesign.**

---

## 21) Hard-coded Colors Audit

افحص وأزل الاستخدامات غير الضرورية لـ:

```text
Color(...)
Colors.white
Colors.black
Colors.green
```

واستبدلها بـSemantic Theme Tokens.

### الاستثناءات
- Brand Gold
- Quran colors
- Error
- Warning
- حالات خاصة بالـBrand

---

## 22) Flutter Theme Mapping المقترح

```dart
const darkColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF0D7564),
  onPrimary: Color(0xFFF2F5F2),
  primaryContainer: Color(0xFF174D43),
  onPrimaryContainer: Color(0xFFF2F5F2),

  secondary: Color(0xFF33C6AE),
  onSecondary: Color(0xFF07110E),
  secondaryContainer: Color(0xFF1A3028),
  onSecondaryContainer: Color(0xFFD7E0DC),

  error: Color(0xFFD9676E),
  onError: Color(0xFFF2F5F2),

  surface: Color(0xFF0E1C17),
  onSurface: Color(0xFFD7E0DC),

  outline: Color(0xFF294239),

  // Adjust remaining required ColorScheme fields
  // according to the Flutter version used in the project.
);
```

يفضل إنشاء ThemeExtension إضافية لـ:

```text
surfaceContainer
surfaceVariant
accentMint
brandGold
warning
textSecondary
textDisabled
bottomNavBackground
quranPaper
quranText
quranGold
```

---

## 23) QA Checklist

يجب التأكد من:

- عدم وجود Pure Black.
- عدم وجود Neon Teal.
- Cards واضحة عن Background.
- Inputs واضحة عن Cards.
- Secondary Text مقروء.
- الصور غير مظلمة بدون سبب.
- Hero image تبقى ظاهرة.
- Gold accents محفوظة.
- Quran page تبقى warm/light.
- لا تتغير Geometry بين Light وDark.
- Bottom navigation لا تغطي المحتوى.
- Contrast جيد في جميع النصوص المهمة.

---

## النتيجة المطلوبة

Dark Mode يجب أن يبدو:

**هادئ — فاخر — روحاني — احترافي — مرتبط مباشرة بالـLight Mode**

وليس:

**أسود + أخضر موحد + تركواز نيون.**
