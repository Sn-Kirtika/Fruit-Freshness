import streamlit as st
import pandas as pd
import numpy as np
import os
from PIL import Image
import plotly.express as px

# -----------------------------
# CONFIGURATION
# -----------------------------
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data")
STATS_DIR = os.path.join(DATA_DIR, "stats")
INPUT_DIR = os.path.join(DATA_DIR, "input")
ARCHIVE_DIR = os.path.join(DATA_DIR, "archive")
PRED_CSV = os.path.join(DATA_DIR, "predictions.csv")

CLASSES = ["Rotten", "Fresh"]

CANVA_GREEN = "#1E7046"
CANVA_BEIGE = "#D6D1C1"

# -----------------------------
# PAGE CONFIG
# -----------------------------
st.set_page_config(
    page_title="Fruit Freshness Dashboard",
    layout="wide"
)

# -----------------------------
# CUSTOM CSS
# -----------------------------
st.markdown(f"""
<style>
.stApp {{ background-color: {CANVA_BEIGE}; }}

header[data-testid="stHeader"] {{ background-color: {CANVA_GREEN} !important; }}

h1, h2, h3, p, span, label {{ color: {CANVA_GREEN} !important; }}

[data-testid="stMetricValue"] {{
    background-color: white;
    color: {CANVA_GREEN} !important;
    border: 1px solid {CANVA_GREEN};
    border-radius: 10px;
    padding: 10px;
}}

</style>
""", unsafe_allow_html=True)

# -----------------------------
# DATA LOADING
# -----------------------------
def load_predictions():
    if os.path.exists(PRED_CSV):
        return pd.read_csv(PRED_CSV)
    return pd.DataFrame(columns=["Prediction", "class"])


# -----------------------------
# HEADER
# -----------------------------
st.title("🍎 Fruit Freshness Dashboard")

# -----------------------------
# DATASET STATS
# -----------------------------
st.header("📊 Dataset Overview")

col1, col2, col3 = st.columns(3)
col1.metric("Total Images", 14165)
col2.metric("Rotten Set", 7884)
col3.metric("Fresh Set", 6281)

# -----------------------------
# PIE / DISTRIBUTION
# -----------------------------
st.subheader("📦 Dataset Split")

split_df = pd.DataFrame({
    "Set": ["Rotten", "Fresh"],
    "Count": [7884, 6281]
})

fig_split = px.pie(
    split_df,
    values="Count",
    names="Set",
    hole=0.4,
    color_discrete_sequence=[CANVA_GREEN, "#7DBE6F"]
)
fig_split.update_layout(
    paper_bgcolor="rgba(0,0,0,0)",
    font_color=CANVA_GREEN
)
st.plotly_chart(fig_split, use_container_width=True)

# -----------------------------
# LOAD PREDICTIONS
# -----------------------------
df_preds = load_predictions()

st.header("🎯 Model Analysis (Binary Classification)")

if not df_preds.empty:

    # -----------------------------
    # METRICS
    # -----------------------------
    #col1, col2, col3 = st.columns(3)

    #col1.metric("Mean Confidence", f"{df_preds['Prediction'].mean():.2%}")
    #col2.metric("Median Confidence", f"{df_preds['Prediction'].median():.2%}")
    #col3.metric("Std Dev", f"{df_preds['Prediction'].std():.3f}")

    # -----------------------------
    # CONFIDENCE DISTRIBUTION
    # -----------------------------
    st.subheader("📊 Prediction Spread by Class")

# Better representation: distribution split by predicted class
fig_hist = px.histogram(
    df_preds,
    x="Prediction",
    color="Category",
    nbins=30,
    barmode="overlay",
    opacity=0.6,
    color_discrete_sequence=[CANVA_GREEN, "#7DBE6F"]
)

# Decision threshold
fig_hist.add_vline(x=0.5, line_dash="dash", line_color="red")

fig_hist.update_layout(
    paper_bgcolor="rgba(0,0,0,0)",
    plot_bgcolor="rgba(0,0,0,0)",
    font_color=CANVA_GREEN
)

st.plotly_chart(fig_hist, use_container_width=True)

# -----------------------------
# CLASS DISTRIBUTION
# -----------------------------
st.subheader("📊 Prediction Distribution")

# Use existing label column from CSV
label_col = "Category"

if label_col in df_preds.columns:
    class_counts = df_preds[label_col].value_counts().reset_index()
    class_counts.columns = ["Class", "Count"]

    fig_bar = px.bar(
        class_counts,
        x="Class",
        y="Count",
        color="Class",
        color_discrete_sequence=[CANVA_GREEN, "#7DBE6F"]
    )

    fig_bar.update_layout(
        paper_bgcolor="rgba(0,0,0,0)",
        plot_bgcolor="rgba(0,0,0,0)",
        font_color=CANVA_GREEN,
        showlegend=False
    )

    st.plotly_chart(fig_bar, use_container_width=True)
else:
    st.info("No Category column found in predictions CSV.")

st.header("📤 Upload Image for Prediction")


uploaded_files = st.file_uploader(
    "Upload fruit images",
    type=["jpg", "jpeg", "png"],
    accept_multiple_files=True
)

if uploaded_files:
    if not os.path.exists(INPUT_DIR):
        os.makedirs(INPUT_DIR)

    cols = st.columns(3)

    for i, file in enumerate(uploaded_files):

        img = Image.open(file)

        # Save image for Scala pipeline
        save_path = os.path.join(INPUT_DIR, file.name)
        with open(save_path, "wb") as f:
            f.write(file.getbuffer())

        with cols[i % 3]:
            st.image(img, use_container_width=True)
            st.success("Sent to Scala pipeline for prediction")

st.markdown("---")

df_gallery = df_preds.copy()

img_col = "File Name"
label_col = "Category"
score_col = "Prediction"

st.header("🖼️ Predicted Images Gallery")

if not df_gallery.empty and img_col in df_gallery.columns:

    # Sort latest first
    df_gallery = df_gallery.iloc[::-1]

    cols = st.columns(4)

    for i, row in df_gallery.iterrows():

        img_path = os.path.join(ARCHIVE_DIR, row[img_col])

        with cols[i % 4]:

            if os.path.exists(img_path):
                st.image(img_path, use_container_width=True)
            else:
                st.warning("Image not found")

            st.write(f"**{row[label_col]}**")

            if score_col in df_gallery.columns:
                st.caption(f"Predicted Value: {row[score_col]}")

else:
    st.info("No predicted images available yet.")

st.caption("Binary classification dashboard: Rotten vs Fresh fruit detection")