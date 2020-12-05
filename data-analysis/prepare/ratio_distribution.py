#%%
import pandas as pd
import numpy as np
from scipy.optimize import curve_fit


def get_ratio_threshold(basecase,split):
    base_case = basecase
    drt_trips = pd.read_csv(base_case)
    drt_trips['dis'] = drt_trips['unsharedDrtDistance'].apply(lambda x: (int)(x / 1000))
    dis_ratio = drt_trips.groupby(by=['dis'])['ratio'].describe(percentiles=split)
    pct = dis_ratio.columns[4:-1]

    con = dis_ratio['count'] > 50
    f = []
    for p in pct:
        f.append(curve_fit(func, dis_ratio[con].index, dis_ratio.loc[con, p])[0])
    return f


def func(x, a, b, c):
    return a * np.exp(-b * x) + c

#%%

base_case = '/Users/meng/work/smart-drt-pricing-paper/snz-berlin/output/ratio_info/Vu/Vu_DRT-0-a.1000.info_org.matsim.smartDrtPricing.SmartDrtFareComputation.csv'
split= [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9]
ratio_split = get_ratio_threshold(base_case,split)

#%%
import matplotlib.pyplot as plt


x = np.arange(0,40,1)
for ff in ratio_split:
    plt.plot(x,func(x,ff[0],ff[1],ff[2]))

plt.show()
