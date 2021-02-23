# %%
import pandas as pd
import numpy as np
from scipy.optimize import curve_fit
import matplotlib.pyplot as plt

# %%
folder = "/Volumes/Macintosh HD/Users/meng/work/smart-drt-pricing-paper/open-berlin-scenario/"
drt_trips_file = folder + "drt_trips_info/sdp-0.ini.info_org.matsim.smartDrtPricing.prepare.EstimatePtTravelTimeEventHandler-2.csv"
drt_trips = pd.read_csv(drt_trips_file)
# %%
split = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
drt_trips['dis'] = drt_trips['beelineTravelDistance'].apply(lambda x: (int)(x / 1000))
dis_ratio = drt_trips.groupby(by=['dis'])['ratio'].describe(percentiles=split)
# %%
pct = dis_ratio.columns[4:-1]
con = (dis_ratio['count'] > 50) & (dis_ratio.index > 0)


def func(x, a, b, c):
    return a + b * np.exp(-c * x)


f = []
for p in pct:
    index = dis_ratio.loc[con, p].index
    values = dis_ratio.loc[con, p].values
    f.append(curve_fit(func, index, values))

# %%
x = np.arange(0, 10, 1)
ratio_split = f
for ff in ratio_split:
    ff = ff[0]
    plt.plot(x, func(x, ff[0], ff[1], ff[2]))

plt.show()

# %%
re = []
pp = []
for x in np.arange(0, len(ratio_split)):
    p = ratio_split[x][0]
    pp.append(p)
    drt_trips['ratio_threshold'] = drt_trips['beelineTravelDistance'].apply(lambda x: func(x / 1000, p[0], p[1], p[2]))
    con = drt_trips['ratio'] < drt_trips['ratio_threshold']
    pct = drt_trips[con].shape[0] / drt_trips.shape[0]
    re.append(pct)
